package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.StatusForService;
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
 * {@link ForwardPassServiceController} and {@link ReversePassServiceController}
 * read and write the status cache to determine the state change to make.
 * <p>
 * A client application can listen to status updates by calling {@link ServiceManager#registerStatusListener(Consumer)}
 */
public class ServiceStatusCache implements Named {

    private Consumer<List<StatusForService>> statusListener = (strings -> {
    });
    private final Map<String, Service.Status> serviceStatusMap = new HashMap<>();

    public Service.Status getStatus(String name) {
        return serviceStatusMap.get(name);
    }

    public void setServiceStatus(String name, Service.Status status) {
        serviceStatusMap.put(name, status);
    }

    /**
     * Injection point for external RegisterStatusListener events, Fluxtion will route events to this instance.
     * <p>
     * Upon registration the service status is published
     *
     * @param listener contains the status listener
     */
    @EventHandler(propagate = false)
    public void registerStatusListener(ServiceManager.RegisterStatusListener listener) {
        statusListener = listener.getStatusListener();
        publishStatus();
    }

    /**
     * Injection point for external publishStatusRequest events, Fluxtion will route events to this instance.
     * <p>
     * On receiving the event the instance will publish the current service status
     *
     * @param publishStatusRequest contains the status listener
     */
    @EventHandler(propagate = false)
    public void publishCurrentStatus(GraphEvent.PublishStatus publishStatusRequest) {
        publishStatus();
    }

    /**
     * If any service dependencies have changed, publishes the current service status.
     *
     * @return flag indicating this node has changed and should notify child nodes of the change
     */
    @OnEvent
    public boolean publishStatus() {
        statusListener.accept(
                serviceStatusMap.entrySet().stream()
                        .map(e -> new StatusForService(e.getKey(), e.getValue()))
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
