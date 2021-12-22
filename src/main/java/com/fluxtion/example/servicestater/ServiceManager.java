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
