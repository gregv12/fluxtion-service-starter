/*
 * Copyright (c) Greg Higgins 2021.
 *
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.gnu.org/licenses/agpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.fluxtion.example.servicestater.graph;

import com.fluxtion.compiler.EventProcessorConfig;
import com.fluxtion.compiler.Fluxtion;
import com.fluxtion.example.servicestater.*;
import com.fluxtion.example.servicestater.graph.GraphEvent.RemoveService;
import com.fluxtion.example.servicestater.helpers.Slf4JAuditLogger;
import com.fluxtion.example.servicestater.helpers.SynchronousTaskExecutor;
import com.fluxtion.runtime.EventProcessor;
import com.fluxtion.runtime.audit.EventLogControlEvent;
import lombok.Synchronized;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
public class FluxtionServiceManager implements ServiceManager, ServiceQuery {

    public static final String START_SUFFIX = "_start";
    public static final String STOP_SUFFIX = "_stop";
    private final Map<String, ServiceController> managedStartServices = new HashMap<>();
    private final TaskWrapperPublisher taskWrapperPublisher = new TaskWrapperPublisher();
    private final ServiceStatusRecordCache serviceStatusRecordCache = new ServiceStatusRecordCache();
    private final DelegatingTaskExecutor taskExecutor = new DelegatingTaskExecutor();
    private EventProcessor startProcessor;
    private boolean addAudit = true;
    private boolean compile = true;
    private boolean triggerDependentsOnStartNotification = false;
    private boolean triggerDependentsOnStopNotification = false;

    public static String toStartServiceName(String serviceName) {
        return serviceName + START_SUFFIX;
    }

    public static String toStopServiceName(String serviceName) {
        return serviceName + STOP_SUFFIX;
    }

    @Override
    public FluxtionServiceManager addService(Service... serviceToAdd) {
        Objects.requireNonNull(startProcessor);
        Objects.requireNonNull(serviceToAdd);
        if (compile) {
            throw new UnsupportedOperationException("only interpreted service graphs can be added to after initial build");
        }
        Set<Service> services = Arrays.stream(serviceToAdd).collect(Collectors.toSet());
        if (!services.isEmpty()) {
            serviceStatusRecordCache.rebuildingMode();
            services.forEach(this::addServicesToMap);//change to recursive lookup
            services.forEach(this::setServiceDependencies);//use the recursive list here
            startProcessor = new SynchronizedEventProcessor(Fluxtion.interpret(this::serviceStarter));
            postBuild(serviceToAdd);
            serviceStatusRecordCache.normalMode();
        }
        return this;
    }

    @Override
    public FluxtionServiceManager removeService(String... servicesToRemove) {
        Objects.requireNonNull(startProcessor);
        Objects.requireNonNull(servicesToRemove);
        if (compile) {
            throw new UnsupportedOperationException("only interpreted service graphs can be removed from after initial build");
        }
        Arrays.stream(servicesToRemove).forEach(this::stopService);
        Arrays.stream(servicesToRemove).map(RemoveService::new).forEach(startProcessor::onEvent);
        Arrays.stream(servicesToRemove).map(FluxtionServiceManager::toStartServiceName).forEach(managedStartServices::remove);
        Arrays.stream(servicesToRemove).map(FluxtionServiceManager::toStopServiceName).forEach(managedStartServices::remove);
        startProcessor = new SynchronizedEventProcessor(Fluxtion.interpret(this::serviceStarter));
        serviceStatusRecordCache.rebuildingMode();
        postBuild();
        serviceStatusRecordCache.normalMode();
        return this;
    }

    public FluxtionServiceManager buildServiceController(Service... serviceList) {
        preBuild(serviceList);
        if (compile) {
            startProcessor = new SynchronizedEventProcessor(Fluxtion.compile(this::serviceStarter));
        } else {
            startProcessor = new SynchronizedEventProcessor(Fluxtion.interpret(this::serviceStarter));
        }
        postBuild(serviceList);
        return this;
    }

    public FluxtionServiceManager buildServiceControllerAot(
            String className,
            String packageName,
            Service... serviceList) {
        preBuild(serviceList);
        startProcessor = new SynchronizedEventProcessor(Fluxtion.compileAot(this::serviceStarter, packageName, className));
        postBuild(serviceList);
        return this;
    }

    public FluxtionServiceManager buildServiceControllerAot(
            String outputDirectory,
            String className,
            String packageName,
            Service... serviceList) {
        preBuild(serviceList);
        startProcessor = Fluxtion.compile(this::serviceStarter, compilerCfg -> {
            compilerCfg.setOutputDirectory(outputDirectory);
            compilerCfg.setPackageName(packageName.trim());
            compilerCfg.setClassName(className.trim());
        });
        postBuild(serviceList);
        return this;
    }

    public FluxtionServiceManager useProcessor(EventProcessor processor) {
        startProcessor = processor;
        postBuild();
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
    public void failFastOnTaskException(boolean failFastFlag) {
        taskExecutor.failFast(failFastFlag);
    }

    @Override
    public void publishSystemStatus() {
        startProcessor.onEvent(new GraphEvent.PublishStatus());
    }

    @Override
    public void bindObjectToService(String serviceName, Object objectToBind) {
        new GraphEvent.RegisterWrappedInstance(serviceName, objectToBind);
    }

    @Override
    public void serviceStarted(String serviceName) {
        log.info("notified service started;'{}'", serviceName);
        GraphEvent.NotifyServiceStarted notifyServiceStarted = new GraphEvent.NotifyServiceStarted(serviceName);
        log.debug(notifyServiceStarted.toString());
        if (triggerDependentsOnStartNotification) {
            log.info("triggering start for dependencies");
            startService(serviceName);
        }
        startProcessor.onEvent(notifyServiceStarted);
        taskExecutor.publishTasksToDelegate();
    }

    @Override
    public void serviceStopped(String serviceName) {
        log.info("notified service stopped;'{}'", serviceName);
        GraphEvent.NotifyServiceStopped notifyServiceStopped = new GraphEvent.NotifyServiceStopped(serviceName);
        log.info(notifyServiceStopped.toString());
        if (triggerDependentsOnStopNotification) {
            log.info("triggering stop for dependencies");
            stopService(serviceName);
        }
        startProcessor.onEvent(notifyServiceStopped);
        taskExecutor.publishTasksToDelegate();
    }

    @Override
    public void triggerDependentsOnStartNotification(boolean triggerDependentsOnStart) {
        this.triggerDependentsOnStartNotification = triggerDependentsOnStart;
    }

    @Override
    public void triggerDependentsOnStopNotification(boolean triggerDependentsOnStop) {
        this.triggerDependentsOnStopNotification = triggerDependentsOnStop;
    }

    @Override
    public void triggerDependentsOnNotification(boolean triggerDependents) {
        triggerDependentsOnStartNotification(triggerDependents);
        triggerDependentsOnStopNotification(triggerDependents);
    }

    @Override
    public void triggerNotificationOnSuccessfulTaskExecution(boolean triggerNotificationOnSuccessfulTaskExecution) {
        taskExecutor.setTriggerNotificationOnSuccessfulTaskExecution(triggerNotificationOnSuccessfulTaskExecution);
    }

    public void triggerNotificationAfterTaskExecution(boolean triggerNotificationAfterTaskExecution) {
        if (triggerNotificationAfterTaskExecution) {
            failFastOnTaskException(false);
        }
        taskExecutor.setTriggerNotificationAfterTaskExecution(triggerNotificationAfterTaskExecution);
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
        forwardPassServiceController.setWrappedInstance(s.getWrappedInstance());
        ReversePassServiceController reversePassServiceController = new ReversePassServiceController(s.getName(), taskWrapperPublisher, serviceStatusRecordCache);
        reversePassServiceController.setStartTask(s.getStartTask());
        reversePassServiceController.setStopTask(s.getStopTask());
        reversePassServiceController.setWrappedInstance(s.getWrappedInstance());
        managedStartServices.put(forwardPassServiceController.getName(), forwardPassServiceController);
        managedStartServices.put(reversePassServiceController.getName(), reversePassServiceController);
    }

    private void setServiceDependencies(Service service) {
        ServiceController controller = managedStartServices.get(toStartServiceName(service.getName()));
        controller.setDependents(
                service.getServiceListThatRequireMe().stream()
                        .map(Service::getName)
                        .map(FluxtionServiceManager::toStartServiceName)
                        .map(managedStartServices::get)
                        .collect(Collectors.toList())
        );
        //assign dependencies
        final ServiceController startController = controller;
        service.getRequiredServiceList().stream()
                .map(Service::getName)
                .map(FluxtionServiceManager::toStartServiceName)
                .map(managedStartServices::get)
                .forEach(s -> s.addDependent(startController));


        //reverse controller
        controller = managedStartServices.get(toStopServiceName(service.getName()));
        final ServiceController stopController = controller;
        service.getRequiredServiceList().stream()
                .map(Service::getName)
                .map(FluxtionServiceManager::toStopServiceName)
                .map(managedStartServices::get)
                .forEach(stopController::addDependent);


        //
        service.getServiceListThatRequireMe().stream()
                .map(Service::getName)
                .map(FluxtionServiceManager::toStopServiceName)
                .map(managedStartServices::get)
                .forEach(s -> s.addDependent(stopController));
    }

    private void serviceStarter(EventProcessorConfig cfg) {
        managedStartServices.values().forEach(cfg::addNode);
        cfg.addNode(taskWrapperPublisher);
        if (addAudit) {
            cfg.addEventAudit(EventLogControlEvent.LogLevel.INFO);
        }
    }

    private void preBuild(Service... serviceList) {
        Objects.requireNonNull(serviceList);
        managedStartServices.clear();
        Arrays.stream(serviceList).forEach(this::addServicesToMap);//change to recursive lookup
        Arrays.stream(serviceList).forEach(this::setServiceDependencies);//use the recursive list here
    }

    private void postBuild(Service... serviceToAdd) {
        startProcessor.init();
        startProcessor.onEvent(new EventLogControlEvent(new Slf4JAuditLogger()));
        for (Service service : serviceToAdd) {
            if(service.getWrappedInstance() != null){
                startProcessor.onEvent(new GraphEvent.RegisterWrappedInstance(service.getName(), service.getWrappedInstance()));
            }
        }
        startProcessor.onEvent(new RegisterCommandProcessor(taskExecutor));
    }

    @Override
    public void startOrder(Consumer<ServiceOrderRecord<?>> serviceConsumer) {
        startProcessor.getExportedService(ServiceQuery.class).startOrder(serviceConsumer);
    }

    @Override
    public void stopOrder(Consumer<ServiceOrderRecord<?>> serviceConsumer) {
        startProcessor.getExportedService(ServiceQuery.class).stopOrder(serviceConsumer);
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

        @Override
        public <T> T getExportedService(Class<T> exportedServiceClass) {
            return delegate.getExportedService(exportedServiceClass);
        }
    }


    class DelegatingTaskExecutor implements TaskWrapper.TaskExecutor {
        private transient final List<TaskWrapper> tasks = new ArrayList<>();
        private TaskWrapper.TaskExecutor delegate;
        private boolean triggerNotificationOnSuccessfulTaskExecution = false;
        private boolean triggerNotificationAfterTaskExecution = false;
        private boolean failFastFlag = true;

        public DelegatingTaskExecutor() {
            delegate = new SynchronousTaskExecutor(failFastFlag);
        }

        public void setDelegate(TaskWrapper.TaskExecutor delegate) {
            Objects.requireNonNull(delegate);
            this.delegate = delegate;
            delegate.failFast(failFastFlag);
        }

        public void setTriggerNotificationOnSuccessfulTaskExecution(boolean triggerNotificationOnSuccessfulTaskExecution) {
            this.triggerNotificationOnSuccessfulTaskExecution = triggerNotificationOnSuccessfulTaskExecution;
        }

        public void setTriggerNotificationAfterTaskExecution(boolean triggerNotificationAfterTaskExecution) {
            if (triggerNotificationAfterTaskExecution) {
                failFastFlag = false;
                triggerNotificationOnSuccessfulTaskExecution = true;
            }
            this.triggerNotificationAfterTaskExecution = triggerNotificationAfterTaskExecution;
        }

        @Override
        public void close() throws Exception {
            delegate.close();
        }

        @Override
        public void accept(List<TaskWrapper> taskWrappers) {
            tasks.clear();
            if (triggerNotificationOnSuccessfulTaskExecution) {
                tasks.addAll(
                        taskWrappers.stream()
                                .map(t -> new NotifyOnSuccessTaskWrapper(t, FluxtionServiceManager.this, triggerNotificationAfterTaskExecution))
                                .collect(Collectors.toList())
                );
            } else {
                tasks.addAll(taskWrappers);
            }
        }

        public void publishTasksToDelegate() {
            if (!tasks.isEmpty()) {
                List<TaskWrapper> tempTasks = new ArrayList<>(tasks);
                tasks.clear();
                delegate.accept(tempTasks);
            }
        }

        @Override
        public void failFast(boolean failFastFlag) {
            this.failFastFlag = failFastFlag;
            delegate.failFast(failFastFlag);
        }
    }
}
