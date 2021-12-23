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
 *
 */
public interface ServiceManager {

    static ServiceManager compiledServiceManager(Service... serviceList){
        FluxtionServiceManager fluxtionServiceManager = new FluxtionServiceManager();
        fluxtionServiceManager.compiled(true);
        fluxtionServiceManager.buildServiceController(serviceList);
        return fluxtionServiceManager;
    }

    static ServiceManager interpretedServiceManager(Service... serviceList){
        FluxtionServiceManager fluxtionServiceManager = new FluxtionServiceManager();
        fluxtionServiceManager.compiled(false);
        fluxtionServiceManager.buildServiceController(serviceList);
        return fluxtionServiceManager;
    }

    static ServiceManagerServer asServer(ServiceManager serviceManager){
        return new ServiceManagerServer(serviceManager);
    }
    void startService(String serviceName);

    void stopService(String serviceName);

    void serviceStarted(String serviceName);

    void serviceStopped(String serviceName);

    void startAllServices();

    void stopAllServices();

    void publishSystemStatus();

    void shutdown();

    void traceMethodCalls(boolean traceOn);

    void registerTaskExecutor(TaskWrapper.TaskExecutor commandProcessor);

    void registerStatusListener(Consumer<List<ServiceStatusRecord>> statusUpdateListener);

}
