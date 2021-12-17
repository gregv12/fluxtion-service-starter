package com.fluxtion.example.servicestater.impl;

import com.fluxtion.example.servicestater.ServiceEvent.PublishStatus;
import com.fluxtion.example.servicestater.ServiceEvent.RegisterStatusListener;
import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.OnEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Publishes the state of the services as recorded by the graph at a point in time. Clients register a service
 */
public class StatusPublisher implements Named {

    private final List<ServiceController> monitoredServices;
    private Consumer<List<String>> statusListener = (strings -> {});

    public StatusPublisher(List<ServiceController> monitoredServices) {
        this.monitoredServices = monitoredServices;
    }

    /**
     * Injection point for external RegisterStatusListener events, Fluxtion will route events to this instance.
     *
     * @param listener contains the status listener
     */
    @EventHandler
    public void registerStatusListener(RegisterStatusListener listener) {
        statusListener = listener.getStatusListener();
        publishStatus();
    }

    /**
     * Injection point for external publishStatusRequest events, Fluxtion will route events to this instance.
     *
     * On receiving the event the instance will publish the current service status to a
     *
     * @param publishStatusRequest contains the status listener
     */
    @EventHandler(propagate = false)
    public void publishCurrentStatus(PublishStatus publishStatusRequest){
        publishStatus();
    }

    @OnEvent
    public boolean publishStatus() {
        statusListener.accept(
                monitoredServices.stream()
                        .map(s -> s.getName() + " - " + s.getStatus())
                        .collect(Collectors.toList())
        );
        return false;
    }

    @Override
    public String getName() {
        return "statusPublisher";
    }
}
