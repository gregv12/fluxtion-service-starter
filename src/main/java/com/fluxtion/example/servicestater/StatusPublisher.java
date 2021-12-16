package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.ServiceEvent.PublishStatus;
import com.fluxtion.example.servicestater.ServiceEvent.RegisterStatusListener;
import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.OnEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StatusPublisher implements Named {

    private final List<ServiceController> monitoredServices;
    private Consumer<List<String>> statusListener = (strings -> {});

    public StatusPublisher(List<ServiceController> monitoredServices) {
        this.monitoredServices = monitoredServices;
    }

    @EventHandler
    public void registerStatusListener(RegisterStatusListener listener) {
        statusListener = listener.getStatusListener();
        publishStatus();
    }

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
