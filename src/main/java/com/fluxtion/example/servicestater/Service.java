package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.graph.ForwardPassServiceController;
import com.fluxtion.example.servicestater.graph.ReversePassServiceController;
import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.partition.LambdaReflection.SerializableRunnable;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
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
@Builder(builderMethodName = "hiddenBuilder")
@ToString
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

    @NonNull
    private final String name;
    @Nullable
    private final List<Service> servicesThatRequireMe;
    @Nullable
    private final List<Service> requiredServices;
    @Nullable
    private final SerializableRunnable startTask;
    @Nullable
    private final SerializableRunnable stopTask;

    public static ServiceBuilder builder(String name){
        return hiddenBuilder().name(name);
    }

    public List<Service> getServicesThatRequireMe() {
        return servicesThatRequireMe ==null?Collections.emptyList(): servicesThatRequireMe;
    }

    public List<Service> getRequiredServices() {
        return requiredServices ==null?Collections.emptyList(): requiredServices;
    }

    @Nullable
    public SerializableRunnable getStartTask() {
        return startTask;
    }

    @Nullable
    public SerializableRunnable getStopTask() {
        return stopTask;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

}
