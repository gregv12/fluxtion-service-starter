package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.helpers.ServiceTaskExecutor;
import com.fluxtion.example.servicestater.helpers.PublishStatusToConsole;
import com.fluxtion.example.servicestater.helpers.CliTestClient;
import org.junit.jupiter.api.Test;

class ServiceManagerTest {

    @Test
    void buildSystemController() {

        //replace with JSON/YAML
        Service svc_1 = new Service("svc_1", CliTestClient::notifyStart, null);
        Service svc_2 = new Service("svc_2", svc_1);
        Service svc_A = new Service("svc_A");
        Service svc_B = new Service("svc_B", svc_A);
        //joined service
        Service svc_2BJoined = new Service("svc_2BJoined", svc_2, svc_B);

        //build and register outputs
        ServiceManager serviceManager = new ServiceManager();
        serviceManager.buildSystemController(svc_1, svc_2, svc_A, svc_B, svc_2BJoined);
//        serviceManager.traceMethodCalls(false);
        serviceManager.registerTaskExecutor(new ServiceTaskExecutor());
        serviceManager.registerStatusListener(new PublishStatusToConsole());


        //start the service manager
        serviceManager.startAllServices();

        //interact with the service
        serviceManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_1"));
        serviceManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_2"));
        serviceManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_A"));
        serviceManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_B"));
        //status query
        serviceManager.publishAllServiceStatus();

        serviceManager.stopAllServices();
    }
}