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

import com.fluxtion.example.servicestater.graph.FluxtionServiceManager;
import com.fluxtion.example.servicestater.graph.ServiceManagerServer;

import java.util.List;
import java.util.function.Consumer;

/**
 * Controls a set of {@link Service}'s
 */
public interface ServiceManager {

    /**
     * Build a compiled version of the service manager. A non-transient service manager that can be used in another
     * process.
     *
     * @param serviceList the services to manage
     * @return ServiceManager controlling client services
     */
    static ServiceManager compiledServiceManager(Service... serviceList) {
        FluxtionServiceManager fluxtionServiceManager = new FluxtionServiceManager();
        fluxtionServiceManager.compiled(true);
        fluxtionServiceManager.buildServiceController(serviceList);
        return fluxtionServiceManager;
    }

    /**
     * Build a transient ServiceManager, when this process ends the {@link ServiceManager} will disappear
     *
     * @param serviceList the services to manage
     * @return ServiceManager controlling client services
     */
    static ServiceManager build(Service... serviceList) {
        FluxtionServiceManager fluxtionServiceManager = new FluxtionServiceManager();
        fluxtionServiceManager.compiled(false);
        fluxtionServiceManager.buildServiceController(serviceList);
        return fluxtionServiceManager;
    }

    /**
     * Wraps a {@link ServiceManager} in a server thread
     *
     * @param serviceManager the wrapped {@link ServiceManager}
     * @return A wrapped serviceManager
     */
    static ServiceManagerServer asServer(ServiceManager serviceManager) {
        return new ServiceManagerServer(serviceManager);
    }


    /**
     * Request from the client to start a service and its dependencies. The {@link ServiceManager} will publish task
     * lists to execute associated with the starting of each connected managed service. Services are started in reverse
     * topological order from the most downstream connected services.
     *
     * @param serviceName the service to start
     */
    void startService(String serviceName);

    /**
     * Request from the client to stop a service and its dependencies. The {@link ServiceManager} will publish task
     * lists to execute associated with the stopping of each connected managed service. Services are stopped in
     * topological order from the most upstream connected services.
     *
     * @param serviceName the service to start
     */
    void stopService(String serviceName);

    /**
     * Clients call this method to notify the {@link ServiceManager} that a service has moved to started state or confirming
     * state of a service.
     *
     * @param serviceName The name of the service that is in the started state
     */
    void serviceStarted(String serviceName);

    /**
     * Clients call this method to notify the {@link ServiceManager} that a service has moved to stopped state or confirming
     * state of a service.
     *
     * @param serviceName The name of the service that is in the stopped state
     */
    void serviceStopped(String serviceName);

    /**
     * A request from the client to start all services.The {@link ServiceManager} will publish task
     * lists to execute associated with the starting of each connected managed service. Services are started in reverse
     * topological order the most downstream services first
     */
    void startAllServices();

    /**
     * A request from the client to staatop rt all services.The {@link ServiceManager} will publish task
     * lists to execute associated with the stopping of each connected managed service. Services are stopped in
     * topological order the most upstream services first
     */
    void stopAllServices();

    /**
     * Publishes the current state of {@link Service}'s managed by this {@link ServiceManager} to the registered
     * status listener, see {@link this#registerStatusListener(Consumer)}
     */
    void publishSystemStatus();

    void shutdown();

    void traceMethodCalls(boolean traceOn);

    void registerTaskExecutor(TaskWrapper.TaskExecutor commandProcessor);

    void registerStatusListener(Consumer<List<ServiceStatusRecord>> statusUpdateListener);

}
