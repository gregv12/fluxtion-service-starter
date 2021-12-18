package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.graph.ForwardPassServiceController;
import com.fluxtion.example.servicestater.graph.ReversePassServiceController;
import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.partition.LambdaReflection.SerializableRunnable;

import java.util.Collections;
import java.util.List;

/**
 * A representation of an external service and its dependencies. This service will be wrapped in the graph and controlled
 * by {@link ForwardPassServiceController} and {@link ReversePassServiceController}. A service can optionally provide start and stop
 * tasks that will be executed when a service moves to the relevant state:
 * <ul>
 *     <li>Entering STARTING from WAITING_TO_START - the start task is executed</li>
 *     <li>Entering STOPPING from STOPPED - the stop task is executed</li>
 * </ul>
 */
public class Service implements Named {

    public enum Status {
        STATUS_UNKNOWN,
        WAITING_FOR_PARENTS_TO_START,
        STARTING,
        STARTED,
        WAITING_FOR_PARENTS_TO_STOP,
        STOPPING,
        STOPPED,
    }
    
    private final String name;
    private final List<Service> dependencies;
    private final SerializableRunnable startTask;
    private final SerializableRunnable stopTask;

    public Service(String name, List<Service> dependencies) {
        this(name, null, null, dependencies );
    }

    public Service(String serviceName, Service... services) {
        this(serviceName, services==null? Collections.emptyList():List.of(services));
    }

    public Service(String serviceName, SerializableRunnable startTask, SerializableRunnable stopTask, Service... services) {
        this(serviceName, startTask, stopTask, services==null? Collections.emptyList():List.of(services));
    }

    public Service(String name, SerializableRunnable startTask, SerializableRunnable stopTask, List<Service> dependencies) {
        this.name = name;
        this.stopTask = stopTask;
        this.dependencies = dependencies;
        this.startTask = startTask;
    }

    public List<Service> getDependencies() {
        return dependencies;
    }

    public SerializableRunnable getStartTask() {
        return startTask;
    }

    public SerializableRunnable getStopTask() {
        return stopTask;
    }

    @Override
    public String getName() {
        return name;
    }


}
