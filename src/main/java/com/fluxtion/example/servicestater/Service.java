/*
 * Copyright (c) Greg Higgins 2021.
 *
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.gnu.org/licenses/agpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.graph.ForwardPassServiceController;
import com.fluxtion.example.servicestater.graph.ReversePassServiceController;
import com.fluxtion.runtime.Named;
import com.fluxtion.runtime.partition.LambdaReflection.SerializableRunnable;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    @NonNull
    private final List<Service> serviceListThatRequireMe;
    @NonNull
    private final List<Service> requiredServiceList;
    @Nullable
    private final SerializableRunnable startTask;
    @Nullable
    private final SerializableRunnable stopTask;

    public static ServiceBuilder builder(String name) {
        return hiddenBuilder().name(name)
                .serviceListThatRequireMe(new ArrayList<>())
                .requiredServiceList(new ArrayList<>());
    }

    public static class ServiceBuilder {

        public ServiceBuilder servicesThatRequireMe(Service... services) {
            Objects.requireNonNull(services, "Requiring me service list cannot be null");
            return this.serviceListThatRequireMe(Arrays.asList(services));
        }

        public ServiceBuilder requiredServices(Service... services) {
            Objects.requireNonNull(services, "Requiring me service list cannot be null");
            return this.requiredServiceList(Arrays.asList(services));
        }
    }

    @NotNull
    public List<Service> getServiceListThatRequireMe() {
        return serviceListThatRequireMe;
    }

    @NotNull
    public List<Service> getRequiredServiceList() {
        return requiredServiceList;
    }

    public Service addRequiredService(Service requireService) {
        Objects.requireNonNull(requiredServiceList).add(requireService);
        return this;
    }

    public Service addServiceThatNeedsMe(Service requireService) {
        Objects.requireNonNull(serviceListThatRequireMe).add(requireService);
        return this;
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
