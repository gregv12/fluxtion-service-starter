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

package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
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
 * A client application can listen to status updates by calling {@link FluxtionServiceManager#registerStatusListener(Consumer)}
 */
public class ServiceStatusRecordCache implements Named {

    private Consumer<List<ServiceStatusRecord>> statusListener = (strings -> {});

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
    public void registerStatusListener(FluxtionServiceManager.RegisterStatusListener listener) {
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
                        .map(e -> new ServiceStatusRecord(e.getKey(), e.getValue()))
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
