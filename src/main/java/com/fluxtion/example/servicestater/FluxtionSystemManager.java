package com.fluxtion.example.servicestater;

import com.fluxtion.compiler.Fluxtion;
import com.fluxtion.compiler.builder.node.SEPConfig;
import com.fluxtion.example.servicestater.ServiceEvent.*;
import com.fluxtion.example.servicestater.impl.*;
import com.fluxtion.runtim.EventProcessor;
import com.fluxtion.runtim.audit.EventLogControlEvent;
import lombok.extern.java.Log;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.fluxtion.example.servicestater.ServiceEvent.*;

/**
 * Manages the lifecycle of a set of external {@link Service}'s. A Service has a set of dependencies and is only stopped/started
 * if their dependencies are all in the correct state. The FluxtionSystemManager receives calls to start/stop as well as
 * updates to service state. After processing the input a set of commands are generated that can be executed. The command
 * list published at the end of the graph cycle has the following characteristic:
 * <ul>
 *     <li>Only commands are published for services whose dependencies are in the correct state</li>
 *     <li>The command execution order is not important</li>
 *     <li>Commands can be executed in parallel</li>
 *     <li>They are not executed by the FluxtionSystemManager, the client code actually invokes the tasks</li>
 * </ul>
 * <p>
 * <p>
 * <p>
 * The FluxtionSystemManager is an entry point for client code to :
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
public class FluxtionSystemManager {

    public static final String START_SUFFIX = "_start";
    public static final String STOP_SUFFIX = "_stop";
    private final Map<String, ServiceController> managedStartServices = new HashMap<>();
    private final CommandPublisher commandPublisher = new CommandPublisher();
    private final SharedServiceStatus sharedServiceStatus = new SharedServiceStatus();
    private EventProcessor startProcessor;

    public void buildSystemController(Service... serviceList) {
        Objects.requireNonNull(serviceList);
        managedStartServices.clear();
        Arrays.stream(serviceList).forEach(this::addServicesToMap);
        Arrays.stream(serviceList).forEach(this::setServiceDependencies);
        startProcessor = Fluxtion.compile(this::serviceStarter);
        startProcessor.init();
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

    public void startAllServices() {
//        ServiceEvent.StartSingleService startSingleServiceAll = new ServiceEvent.StartSingleService("all");
        log.info("start all");
        startProcessor.onEvent(new StartAllServices());
        publishAllServiceStatus();
    }

    public void stopAllServices() {
        log.info("stop all");
        startProcessor.onEvent(new StopAllServices());
        publishAllServiceStatus();
    }

    public void registerCommandProcessor(Consumer<List<Command>> commandProcessor) {
        startProcessor.onEvent(new RegisterCommandProcessor(commandProcessor));
    }

    public void registerStatusListener(Consumer<List<String>> statusUpdateListener) {
        startProcessor.onEvent(new RegisterStatusListener(statusUpdateListener));
    }

    public void publishAllServiceStatus() {
        startProcessor.onEvent(new PublishStatus());
    }

    public void processStatusUpdate(StatusUpdate statusUpdate) {
        log.info(statusUpdate.toString());
        startProcessor.onEvent(statusUpdate);
    }

    private void addServicesToMap(Service s) {
        StartServiceController startServiceController = new StartServiceController(s.getName(), commandPublisher, sharedServiceStatus);
        StopServiceController stopServiceController = new StopServiceController(s.getName(), commandPublisher, sharedServiceStatus);
        managedStartServices.put(startServiceController.getName(), startServiceController);
        managedStartServices.put(stopServiceController.getName(), stopServiceController);
    }

    private void setServiceDependencies(Service service) {
        ServiceController controller = managedStartServices.get(toStartServiceName(service.getName()));
        controller.setDependencies(
                service.getDependencies().stream()
                        .map(Service::getName)
                        .map(FluxtionSystemManager::toStartServiceName)
                        .map(managedStartServices::get)
                        .collect(Collectors.toList())
        );

        controller = managedStartServices.get(toStopServiceName(service.getName()));
        final ServiceController stopController = controller;
        service.getDependencies().stream()
                .map(Service::getName)
                .map(FluxtionSystemManager::toStopServiceName)
                .map(managedStartServices::get)
                .forEach(s -> s.addDependency(stopController));
    }

    private void serviceStarter(SEPConfig cfg) {
        managedStartServices.values().forEach(cfg::addNode);
        cfg.addNode(commandPublisher);
        cfg.addEventAudit(EventLogControlEvent.LogLevel.INFO);
    }

    public static String toStartServiceName(String serviceName) {
        return serviceName + START_SUFFIX;
    }

    public static String toStopServiceName(String serviceName) {
        return serviceName + STOP_SUFFIX;
    }
}
