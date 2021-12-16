package com.fluxtion.example.servicestater;

import com.fluxtion.compiler.Fluxtion;
import com.fluxtion.compiler.builder.node.SEPConfig;
import com.fluxtion.example.servicestater.ServiceController.StartServiceController;
import com.fluxtion.example.servicestater.ServiceEvent.Command;
import com.fluxtion.runtim.EventProcessor;
import com.fluxtion.runtim.audit.EventLogControlEvent;
import lombok.extern.java.Log;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.fluxtion.example.servicestater.ServiceController.StopServiceController;

@Log
public class FluxtionSystemManager {

    public static final String START_SUFFIX = "_start";
    public static final String STOP_SUFFIX = "_stop";
    private final Map<String, ServiceController> managedStartServices = new HashMap<>();
    private final CommandPublisher commandPublisher = new CommandPublisher();
    private final SharedStatus sharedStatus = new SharedStatus();
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

    public void traceMethodCalls(boolean traceOn){
        if(traceOn){
            startProcessor.onEvent(new EventLogControlEvent(EventLogControlEvent.LogLevel.TRACE));
        }else{
            startProcessor.onEvent(new EventLogControlEvent(EventLogControlEvent.LogLevel.WARN));
        }
    }

    public void startServices() {
        ServiceEvent.Start startAll = new ServiceEvent.Start("all");
        log.info("start all " + startAll);
        startProcessor.onEvent(startAll);
        publishAllServiceStatus();
    }

    public void stopServices() {
        ServiceEvent.Stop stopAll = new ServiceEvent.Stop("all");
        log.info("start all " + stopAll);
        startProcessor.onEvent(stopAll);
        publishAllServiceStatus();
    }

    public void registerCommandPublisher(Consumer<List<Command>> commandProcessor) {
        startProcessor.onEvent(new ServiceEvent.RegisterCommandProcessor(commandProcessor));
    }

    public void registerStatusListener(Consumer<List<String>> statusUpdateListener) {
        startProcessor.onEvent(new ServiceEvent.RegisterStatusListener(statusUpdateListener));
    }

    public void publishAllServiceStatus() {
        startProcessor.onEvent(new ServiceEvent.PublishStatus());
    }

    public void processStatusUpdate(ServiceEvent.StatusUpdate statusUpdate) {
        log.info(statusUpdate.toString());
        startProcessor.onEvent(statusUpdate);
    }

    private void addServicesToMap(Service s) {
        StartServiceController startServiceController = new StartServiceController(s.getName(), commandPublisher, sharedStatus);
        StopServiceController stopServiceController = new StopServiceController(s.getName(), commandPublisher, sharedStatus);
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
        ;
    }

    private void serviceStarter(SEPConfig cfg) {
        managedStartServices.values().forEach(cfg::addNode);
        cfg.addNode(commandPublisher);
//        cfg.addEventAudit(EventLogControlEvent.LogLevel.TRACE);
    }

    public static final String toStartServiceName(String serviceName) {
        return serviceName + START_SUFFIX;
    }

    public static final String toStopServiceName(String serviceName) {
        return serviceName + STOP_SUFFIX;
    }
}
