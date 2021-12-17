package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.helpers.PublishCommandsToConsole;
import com.fluxtion.example.servicestater.helpers.PublishStatusToConsole;
import org.junit.jupiter.api.Test;

class FluxtionSystemManagerTest {

    @Test
    void buildSystemController() {

        //replace with JSON/YAML
        Service svc_1 = new Service("svc_1");
        Service svc_2 = new Service("svc_2", svc_1);
        Service svc_A = new Service("svc_A");
        Service svc_B = new Service("svc_B", svc_A);
        //joined service
        Service svc_2BJoined = new Service("svc_2BJoined", svc_2, svc_B);

        //build and register outputs
        FluxtionSystemManager fluxtionSystemManager = new FluxtionSystemManager();
        fluxtionSystemManager.buildSystemController(svc_1, svc_2, svc_A, svc_B, svc_2BJoined);
//        fluxtionSystemManager.traceMethodCalls(false);
        fluxtionSystemManager.registerCommandPublisher(new PublishCommandsToConsole());
        fluxtionSystemManager.registerStatusListener(new PublishStatusToConsole());


        //start the service manager
        fluxtionSystemManager.startServices();

        //interact with the service
        fluxtionSystemManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_1"));
        fluxtionSystemManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_2"));
        fluxtionSystemManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_A"));
        fluxtionSystemManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_B"));
        //status query
        fluxtionSystemManager.publishAllServiceStatus();

        fluxtionSystemManager.stopServices();
    }
}