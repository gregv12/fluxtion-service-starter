package com.fluxtion.example.servicestater.impl;

import com.fluxtion.example.servicestater.ServiceEvent;
import com.fluxtion.example.servicestater.ServiceStatus;
import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.Initialise;
import com.fluxtion.runtim.annotations.OnEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A cache for the current status of the external {@link com.fluxtion.example.servicestater.Service}, both
 * {@link com.fluxtion.example.servicestater.impl.ServiceController.StartServiceController} and {@link com.fluxtion.example.servicestater.impl.ServiceController.StopServiceController}
 * read and write the status cache to determine the state change to make.
 *
 * A client application can listen to status updates by calling {@link com.fluxtion.example.servicestater.FluxtionSystemManager#registerStatusListener(Consumer)}
 */
public class SharedServiceStatus implements Named {

    private Consumer<List<String>> statusListener = (strings -> {});
    private final Map<String, ServiceStatus> serviceStatusMap = new HashMap<>();

    public ServiceStatus getStatus(String name) {
        return serviceStatusMap.get(name);
    }

    public void setServiceStatus(String name, ServiceStatus serviceStatus) {
        serviceStatusMap.put(name, serviceStatus);
    }

    @EventHandler(propagate = false)
    public void registerStatusListener(ServiceEvent.RegisterStatusListener listener) {
        statusListener = listener.getStatusListener();
        publishStatus();
    }

    @EventHandler(propagate = false)
    public void publishCurrentStatus(ServiceEvent.PublishStatus publishStatusRequest) {
        publishStatus();
    }

    @OnEvent
    public boolean publishStatus() {
        statusListener.accept(
                serviceStatusMap.entrySet().stream()
                        .map(e -> e.getKey() + " - " + e.getValue())
                        .collect(Collectors.toList())
        );
        return false;
    }

    @Initialise
    public void init() {
    }

    @Override
    public String getName() {
        return "serviceStatusCache";
    }
}
