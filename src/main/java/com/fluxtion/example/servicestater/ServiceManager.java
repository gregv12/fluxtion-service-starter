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
import com.fluxtion.runtim.EventProcessor;

import java.util.List;
import java.util.function.Consumer;

/**
 * Controls a set of {@link Service}'s
 */
public interface ServiceManager {

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
     * Load an {@link EventProcessor} and wrap as a ServiceManager, this method is for use when a service manager
     * has been compiled aot.
     * @param processor the {@link EventProcessor} to wrap as a SserviceManager
     * @return ServiceManager controlling client services
     */
    static ServiceManager fromProcessor(EventProcessor processor){
        FluxtionServiceManager fluxtionServiceManager = new FluxtionServiceManager();
        fluxtionServiceManager.useProcessor(processor);
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
     * A request from the client to stop all services.The {@link ServiceManager} will publish task
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

    /**
     * Flag to control triggering of start tasks for a service if an unsolicited start notification is received.
     * When set to true, {@link this#serviceStarted(String)} is equivalent to calling {@link this#startService(String)}
     * @param triggerDependentsOnStart flag to control start triggering behaviour
     */
    void triggerDependentsOnStartNotification(boolean triggerDependentsOnStart);

    /**
     * Flag to control triggering of stop tasks for a service if an unsolicited stop notification is received.
     * When set to true, {@link this#serviceStopped(String)} is equivalent to calling {@link this#stopService(String)}
     * @param triggerDependentsOnStop flag to control start triggering behaviour
     */
    void triggerDependentsOnStopNotification(boolean triggerDependentsOnStop);

    /**
     * Flag to control triggering of start and stop tasks for a service if an unsolicited notification is received.
     * When set to true:
     * <ul>
     *     <li>{@link this#serviceStarted(String)} is equivalent to calling {@link this#startService(String)}</li>
     *     <li>{@link this#serviceStopped(String)} is equivalent to calling {@link this#stopService(String)}  </li>
     * </li>
     *
     * @param triggerDependents flag to control start/stop task triggering behaviour
     */
    void triggerDependentsOnNotification(boolean triggerDependents);

    /**
     * Flag to trigger automatic notification to the {@link ServiceManager} if a start/stop task is present and executes
     * without an exception. Calling either:
     * <ul>
     *     <li>{@link ServiceManager#serviceStarted(String)}</li>
     *     <li>{@link ServiceManager#serviceStopped(String)}</li>
     * </ul>
     *
     * otherwise no notification is sent to the {@link ServiceManager}
     * @param triggerNotificationOnSuccessfulTaskExecution flag controlling automatic start/stop notification
     */
    void triggerNotificationOnSuccessfulTaskExecution(boolean triggerNotificationOnSuccessfulTaskExecution);
}
