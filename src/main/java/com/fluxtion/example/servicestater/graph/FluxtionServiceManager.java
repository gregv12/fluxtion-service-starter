package com.fluxtion.example.servicestater.graph;

import com.fluxtion.compiler.Fluxtion;
import com.fluxtion.compiler.builder.node.SEPConfig;
import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import com.fluxtion.example.servicestater.TaskWrapper;
import com.fluxtion.example.servicestater.helpers.Slf4JAuditLogger;
import com.fluxtion.example.servicestater.helpers.SynchronousTaskExecutor;
import com.fluxtion.runtim.EventProcessor;
import com.fluxtion.runtim.audit.EventLogControlEvent;
import lombok.Synchronized;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Manages the lifecycle of a set of external {@link Service}'s. A Service has a set of dependencies and is only stopped/started
 * if their dependencies are all in the correct state. The ServiceManager receives calls to start/stop as well as
 * updates to service state. After processing the input a set of commands are generated that can be executed. The command
 * list published at the end of the graph cycle has the following characteristic:
 * <ul>
 *     <li>Only commands are published for services whose dependencies are in the correct state</li>
 *     <li>The command execution order is not important</li>
 *     <li>Commands can be executed in parallel</li>
 *     <li>They are not executed by the ServiceManager, the client code actually invokes the tasks</li>
 * </ul>
 * <p>
 * <p>
 * <p>
 * The ServiceManager is an entry point for client code to :
 * <ul>
 *     <li>register services</li>
 *     <li>Build a service controller for the whole system</li>
 *     <li>Start/stop system</li>
 *     <li>Register status listeners</li>
 *     <li>Register command processors</li>
 *     <li>Request publish of service status</li>
 *     <li>Post service status updates</li>
 * </ul>
 */
@Slf4j
public class FluxtionServiceManager implements ServiceManager {

    public static final String START_SUFFIX = "_start";
    public static final String STOP_SUFFIX = "_stop";
    private final Map<String, ServiceController> managedStartServices = new HashMap<>();
    private final TaskWrapperPublisher taskWrapperPublisher = new TaskWrapperPublisher();
    private final ServiceStatusRecordCache serviceStatusRecordCache = new ServiceStatusRecordCache();
    private EventProcessor startProcessor;
    private boolean addAudit = true;
    private boolean compile = true;
    private final DelegatingTaskExecutor taskExecutor = new DelegatingTaskExecutor();

    public FluxtionServiceManager buildServiceController(Service... serviceList) {
        Objects.requireNonNull(serviceList);
        managedStartServices.clear();
        Arrays.stream(serviceList).forEach(this::addServicesToMap);//change to recursive lookup
        Arrays.stream(serviceList).forEach(this::setServiceDependencies);//use the recursive list here
        if (compile) {
            startProcessor = new SynchronizedEventProcessor(Fluxtion.compile(this::serviceStarter));
        } else {
            startProcessor = new SynchronizedEventProcessor(Fluxtion.interpret(this::serviceStarter));
        }
        startProcessor.init();
        startProcessor.onEvent(new EventLogControlEvent(new Slf4JAuditLogger()));
        startProcessor.onEvent(new RegisterCommandProcessor(taskExecutor));
        return this;
    }

    @Override
    @Synchronized
    public void shutdown() {
        log.info("shutting down task executor");
        try {
            taskExecutor.close();
        } catch (Exception e) {
            log.warn("failed to shutdown task executor", e);
        }
    }

    @Override
    public void traceMethodCalls(boolean traceOn) {
        if (traceOn) {
            startProcessor.onEvent(new EventLogControlEvent(EventLogControlEvent.LogLevel.TRACE));
        } else {
            startProcessor.onEvent(new EventLogControlEvent(EventLogControlEvent.LogLevel.WARN));
        }
    }

    @Override
    @Synchronized
    public void startService(String serviceName) {
        log.info("start single service:'{}'", serviceName);
        startProcessor.onEvent(new GraphEvent.RequestServiceStart(serviceName));
        taskExecutor.publishTasksToDelegate();
        startProcessor.onEvent(new GraphEvent.PublishStartTask());
        taskExecutor.publishTasksToDelegate();
        publishSystemStatus();
    }

    @Override
    @Synchronized
    public void stopService(String serviceName) {
        log.info("stop single service:'{}'", serviceName);
        startProcessor.onEvent(new GraphEvent.RequestServiceStop(serviceName));
        taskExecutor.publishTasksToDelegate();
        startProcessor.onEvent(new GraphEvent.PublishStopTask());
        taskExecutor.publishTasksToDelegate();
        publishSystemStatus();
    }

    @Override
    public void startAllServices() {
        log.info("start all");
        startProcessor.onEvent(new GraphEvent.RequestStartAll());
        taskExecutor.publishTasksToDelegate();
        startProcessor.onEvent(new GraphEvent.PublishStartTask());
        taskExecutor.publishTasksToDelegate();
        publishSystemStatus();
    }

    @Override
    public void stopAllServices() {
        log.info("stop all");
        startProcessor.onEvent(new GraphEvent.RequestStopAll());
        startProcessor.onEvent(new GraphEvent.PublishStopTask());
        taskExecutor.publishTasksToDelegate();
        publishSystemStatus();
    }

    @Override
    public void registerTaskExecutor(TaskWrapper.TaskExecutor commandProcessor) {
        taskExecutor.setDelegate(commandProcessor);
    }

    @Override
    public void registerStatusListener(Consumer<List<ServiceStatusRecord>> statusUpdateListener) {
        startProcessor.onEvent(new RegisterStatusListener(statusUpdateListener));
    }

    @Override
    public void publishSystemStatus() {
        startProcessor.onEvent(new GraphEvent.PublishStatus());
    }

    @Override
    public void serviceStarted(String serviceName) {
        log.info("notified service started;'{}'", serviceName);
        GraphEvent.NotifyServiceStarted notifyServiceStarted = new GraphEvent.NotifyServiceStarted(serviceName);
        log.debug(notifyServiceStarted.toString());
        startProcessor.onEvent(notifyServiceStarted);
        taskExecutor.publishTasksToDelegate();
    }

    @Override
    public void serviceStopped(String serviceName) {
        log.info("notified service stopped;'{}'", serviceName);
        GraphEvent.NotifyServiceStopped notifyServiceStarted = new GraphEvent.NotifyServiceStopped(serviceName);
        log.info(notifyServiceStarted.toString());
        startProcessor.onEvent(notifyServiceStarted);
        taskExecutor.publishTasksToDelegate();
    }

    public FluxtionServiceManager addAuditLog(boolean addAudit) {
        this.addAudit = addAudit;
        return this;
    }

    public FluxtionServiceManager compiled(boolean compile) {
        this.compile = compile;
        return this;
    }

    private void addServicesToMap(Service s) {
        ForwardPassServiceController forwardPassServiceController = new ForwardPassServiceController(s.getName(), taskWrapperPublisher, serviceStatusRecordCache);
        forwardPassServiceController.setStartTask(s.getStartTask());
        forwardPassServiceController.setStopTask(s.getStopTask());
        ReversePassServiceController reversePassServiceController = new ReversePassServiceController(s.getName(), taskWrapperPublisher, serviceStatusRecordCache);
        reversePassServiceController.setStartTask(s.getStartTask());
        reversePassServiceController.setStopTask(s.getStopTask());
        managedStartServices.put(forwardPassServiceController.getName(), forwardPassServiceController);
        managedStartServices.put(reversePassServiceController.getName(), reversePassServiceController);
    }

    private void setServiceDependencies(Service service) {
        ServiceController controller = managedStartServices.get(toStartServiceName(service.getName()));
        controller.setDependents(
                service.getServicesThatRequireMe().stream()
                        .map(Service::getName)
                        .map(FluxtionServiceManager::toStartServiceName)
                        .map(managedStartServices::get)
                        .collect(Collectors.toList())
        );
        //assign dependencies
        final ServiceController startController = controller;
        service.getRequiredServices().stream()
                .map(Service::getName)
                .map(FluxtionServiceManager::toStartServiceName)
                .map(managedStartServices::get)
                .forEach(s -> s.addDependent(startController));


        //reverse controller
        controller = managedStartServices.get(toStopServiceName(service.getName()));
        final ServiceController stopController = controller;
        service.getRequiredServices().stream()
                .map(Service::getName)
                .map(FluxtionServiceManager::toStopServiceName)
                .map(managedStartServices::get)
                .forEach(stopController::addDependent);


        //
        service.getServicesThatRequireMe().stream()
                .map(Service::getName)
                .map(FluxtionServiceManager::toStopServiceName)
                .map(managedStartServices::get)
                .forEach(s -> s.addDependent(stopController));
    }

    private void serviceStarter(SEPConfig cfg) {
        managedStartServices.values().forEach(cfg::addNode);
        cfg.addNode(taskWrapperPublisher);
        if (addAudit) {
            cfg.addEventAudit(EventLogControlEvent.LogLevel.INFO);
        }
    }

    public static String toStartServiceName(String serviceName) {
        return serviceName + START_SUFFIX;
    }

    public static String toStopServiceName(String serviceName) {
        return serviceName + STOP_SUFFIX;
    }

    @Value
    public static class RegisterCommandProcessor {
        Consumer<List<TaskWrapper>> consumer;
    }

    @Value
    public static class RegisterStatusListener {
        Consumer<List<ServiceStatusRecord>> statusListener;
    }

    @Value
    static class SynchronizedEventProcessor implements EventProcessor {
        EventProcessor delegate;

        @Override
        @Synchronized
        public void onEvent(Object e) {
            delegate.onEvent(e);
        }

        @Override
        @Synchronized
        public void init() {
            delegate.init();
        }

        @Override
        @Synchronized
        public void tearDown() {
            delegate.tearDown();
        }
    }


    static class DelegatingTaskExecutor implements TaskWrapper.TaskExecutor {
        private TaskWrapper.TaskExecutor delegate;
        private transient final List<TaskWrapper> tasks = new ArrayList<>();

        public DelegatingTaskExecutor() {
            delegate = new SynchronousTaskExecutor();
        }

        public void setDelegate(TaskWrapper.TaskExecutor delegate) {
            Objects.requireNonNull(delegate);
            this.delegate = delegate;
        }

        @Override
        public void close() throws Exception {
            delegate.close();
        }

        @Override
        public void accept(List<TaskWrapper> taskWrappers) {
            tasks.clear();
            tasks.addAll(taskWrappers);
        }

        public void publishTasksToDelegate() {
            if (!tasks.isEmpty()) {
                List<TaskWrapper> tempTasks = new ArrayList<>(tasks);
                tasks.clear();
                delegate.accept(tempTasks);
            }
        }
    }
}
