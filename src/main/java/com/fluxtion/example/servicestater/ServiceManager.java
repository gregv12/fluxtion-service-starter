package com.fluxtion.example.servicestater;

import com.fluxtion.compiler.Fluxtion;
import com.fluxtion.compiler.builder.node.SEPConfig;
import com.fluxtion.example.servicestater.graph.*;
import com.fluxtion.runtim.EventProcessor;
import com.fluxtion.runtim.audit.EventLogControlEvent;
import lombok.Value;
import lombok.extern.java.Log;

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
@Log
public class ServiceManager {

    public static final String START_SUFFIX = "_start";
    public static final String STOP_SUFFIX = "_stop";
    private final Map<String, ServiceController> managedStartServices = new HashMap<>();
    private final TaskWrapperPublisher taskWrapperPublisher = new TaskWrapperPublisher();
    private final ServiceStatusCache serviceStatusCache = new ServiceStatusCache();
    private EventProcessor startProcessor;
    private boolean addAudit = true;
    private boolean compile = true;

    public ServiceManager buildServiceController(Service... serviceList) {
        Objects.requireNonNull(serviceList);
        managedStartServices.clear();
        Arrays.stream(serviceList).forEach(this::addServicesToMap);//change to recursive lookup
        Arrays.stream(serviceList).forEach(this::setServiceDependencies);//use the recursive list here
        if(compile){
            startProcessor = Fluxtion.compile(this::serviceStarter);
        }else{
            startProcessor = Fluxtion.interpret(this::serviceStarter);
        }
        startProcessor.init();
        return this;
    }

// TODO: implement dynamic graph building
//    public void addOrUpdateService(Service service) {
//        //rebuild
//    }
//
//    public void removeService(String serviceName) {
//        //rebuild
//    }

    public void traceMethodCalls(boolean traceOn) {
        if (traceOn) {
            startProcessor.onEvent(new EventLogControlEvent(EventLogControlEvent.LogLevel.TRACE));
        } else {
            startProcessor.onEvent(new EventLogControlEvent(EventLogControlEvent.LogLevel.WARN));
        }
    }

    public void startService(String serviceName) {
        log.fine("start single service:" + serviceName);
        startProcessor.onEvent(new GraphEvent.RequestServiceStart(serviceName));
        startProcessor.onEvent(new GraphEvent.PublishStartTask());
        publishAllServiceStatus();
    }

    public void stopService(String serviceName) {
        log.fine("stop single service:" + serviceName);
        startProcessor.onEvent(new GraphEvent.RequestServiceStop(serviceName));
        startProcessor.onEvent(new GraphEvent.PublishStartTask());
        publishAllServiceStatus();
    }

    public void startAllServices() {
        log.fine("start all");
        startProcessor.onEvent(new GraphEvent.RequestStartAll());
        publishAllServiceStatus();
    }

    public void stopAllServices() {
        log.fine("stop all");
        startProcessor.onEvent(new GraphEvent.RequestStopAll());
        publishAllServiceStatus();
    }

    public void registerTaskExecutor(Consumer<List<TaskWrapper>> commandProcessor) {
        startProcessor.onEvent(new RegisterCommandProcessor(commandProcessor));
    }

    public void registerStatusListener(Consumer<List<StatusForService>> statusUpdateListener) {
        startProcessor.onEvent(new RegisterStatusListener(statusUpdateListener));
    }

    public void publishAllServiceStatus() {
        startProcessor.onEvent(new GraphEvent.PublishStatus());
    }

    public void serviceStartedNotification(String serviceName) {
        GraphEvent.NotifyServiceStarted notifyServiceStarted = new GraphEvent.NotifyServiceStarted(serviceName);
        log.fine(notifyServiceStarted.toString());
        startProcessor.onEvent(notifyServiceStarted);
    }

    public void serviceStoppedNotification(String serviceName) {
        GraphEvent.NotifyServiceStopped notifyServiceStarted = new GraphEvent.NotifyServiceStopped(serviceName);
        log.fine(notifyServiceStarted.toString());
        startProcessor.onEvent(notifyServiceStarted);
    }

    public ServiceManager addAuditLog(boolean addAudit){
        this.addAudit = addAudit;
        return this;
    }

    public ServiceManager compiled(boolean compile){
        this.compile = compile;
        return this;
    }

    private void addServicesToMap(Service s) {
        ForwardPassServiceController forwardPassServiceController = new ForwardPassServiceController(s.getName(), taskWrapperPublisher, serviceStatusCache);
        forwardPassServiceController.setStartTask(s.getStartTask());
        forwardPassServiceController.setStopTask(s.getStopTask());
        ReversePassServiceController reversePassServiceController = new ReversePassServiceController(s.getName(), taskWrapperPublisher, serviceStatusCache);
        reversePassServiceController.setStartTask(s.getStartTask());
        reversePassServiceController.setStopTask(s.getStopTask());
        managedStartServices.put(forwardPassServiceController.getName(), forwardPassServiceController);
        managedStartServices.put(reversePassServiceController.getName(), reversePassServiceController);
    }

    private void setServiceDependencies(Service service) {
        ServiceController controller = managedStartServices.get(toStartServiceName(service.getName()));
        controller.setDependencies(
                service.getDependencies().stream()
                        .map(Service::getName)
                        .map(ServiceManager::toStartServiceName)
                        .map(managedStartServices::get)
                        .collect(Collectors.toList())
        );

        controller = managedStartServices.get(toStopServiceName(service.getName()));
        final ServiceController stopController = controller;
        service.getDependencies().stream()
                .map(Service::getName)
                .map(ServiceManager::toStopServiceName)
                .map(managedStartServices::get)
                .forEach(s -> s.addDependency(stopController));
    }

    private void serviceStarter(SEPConfig cfg) {
        managedStartServices.values().forEach(cfg::addNode);
        cfg.addNode(taskWrapperPublisher);
        if(addAudit){
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
        Consumer<List<StatusForService>> statusListener;
    }
}
