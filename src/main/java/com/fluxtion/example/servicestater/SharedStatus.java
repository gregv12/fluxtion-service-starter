package com.fluxtion.example.servicestater;

import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.Initialise;
import com.fluxtion.runtim.annotations.OnEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SharedStatus implements Named {

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
