package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class TriggeringOnStartStopNotificationTest extends BaseServiceStarterTest {

    protected Service svcA;
    protected Service svcB;
    protected Service svcC;
    protected Service svcD;
    protected ServiceManager serviceManager;
    protected HashMap<String, ServiceStatusRecord> statusMap;

    @Test
    public void triggerOnStartNotification(){

        buildGraph();
        serviceManager.triggerDependentsOnStartNotification(true);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcA.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcB.getName(), Service.Status.WAITING_FOR_PARENTS_TO_START);
        updateStatus(statusMap, svcC.getName(), Service.Status.WAITING_FOR_PARENTS_TO_START);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTING);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcD.getName());
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTING);
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTING);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcC.getName());
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcB.getName());
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);
    }


    @Test
    public void triggerOnStopNotification(){

        buildGraph();
        serviceManager.triggerDependentsOnStopNotification(true);
        checkStatusMatch(statusMap);

        serviceManager.serviceStopped(svcD.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.STOPPING);
        updateStatus(statusMap, svcB.getName(), Service.Status.WAITING_FOR_PARENTS_TO_STOP);
        updateStatus(statusMap, svcC.getName(), Service.Status.WAITING_FOR_PARENTS_TO_STOP);
        updateStatus(statusMap, svcD.getName(), Service.Status.STOPPED);
        checkStatusMatch(statusMap);

        serviceManager.serviceStopped(svcA.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.STOPPED);
        updateStatus(statusMap, svcB.getName(), Service.Status.STOPPING);
        updateStatus(statusMap, svcC.getName(), Service.Status.STOPPING);
        checkStatusMatch(statusMap);

        serviceManager.serviceStopped(svcC.getName());
        updateStatus(statusMap, svcC.getName(), Service.Status.STOPPED);
        checkStatusMatch(statusMap);

        serviceManager.serviceStopped(svcB.getName());
        updateStatus(statusMap, svcB.getName(), Service.Status.STOPPED);
        checkStatusMatch(statusMap);
    }

    @Test
    public void triggerOnStartAndStopNotification(){
        ADD_AUDIT_LOG = true;
        buildGraph();
        serviceManager.triggerDependentsOnNotification(true);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcA.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcB.getName(), Service.Status.WAITING_FOR_PARENTS_TO_START);
        updateStatus(statusMap, svcC.getName(), Service.Status.WAITING_FOR_PARENTS_TO_START);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTING);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcD.getName());
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTING);
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTING);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcC.getName());
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcB.getName());
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);


        buildGraph();
        serviceManager.triggerDependentsOnNotification(true);
        checkStatusMatch(statusMap);

        serviceManager.serviceStopped(svcD.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.STOPPING);
        updateStatus(statusMap, svcB.getName(), Service.Status.WAITING_FOR_PARENTS_TO_STOP);
        updateStatus(statusMap, svcC.getName(), Service.Status.WAITING_FOR_PARENTS_TO_STOP);
        updateStatus(statusMap, svcD.getName(), Service.Status.STOPPED);
        checkStatusMatch(statusMap);

        serviceManager.serviceStopped(svcA.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.STOPPED);
        updateStatus(statusMap, svcB.getName(), Service.Status.STOPPING);
        updateStatus(statusMap, svcC.getName(), Service.Status.STOPPING);
        checkStatusMatch(statusMap);

        serviceManager.serviceStopped(svcC.getName());
        updateStatus(statusMap, svcC.getName(), Service.Status.STOPPED);
        checkStatusMatch(statusMap);

        serviceManager.serviceStopped(svcB.getName());
        updateStatus(statusMap, svcB.getName(), Service.Status.STOPPED);
        checkStatusMatch(statusMap);
    }

    @Test
    public void triggerOnStartAndStopNotification_StartFollowedByStop() {
        ADD_AUDIT_LOG = true;
        buildGraph();
        serviceManager.triggerDependentsOnNotification(true);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcA.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcB.getName(), Service.Status.WAITING_FOR_PARENTS_TO_START);
        updateStatus(statusMap, svcC.getName(), Service.Status.WAITING_FOR_PARENTS_TO_START);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTING);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcD.getName());
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTING);
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTING);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcC.getName());
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);

        //lets stop C now
        serviceManager.serviceStopped(svcC.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.STOPPING);
        updateStatus(statusMap, svcC.getName(), Service.Status.STOPPED);
        checkStatusMatch(statusMap);

        serviceManager.serviceStarted(svcB.getName());
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTED);

        serviceManager.serviceStopped(svcA.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.STOPPED);
    }

    protected void buildGraph(){
        svcA = Service.builder("A").build();
        svcB = Service.builder("B").servicesThatRequireMe(svcA).build();
        svcC = Service.builder("C").servicesThatRequireMe(svcA).build();
        svcD = Service.builder("D").servicesThatRequireMe(svcB, svcC).build();

        serviceManager = ServiceManager.build(
                svcA,
                svcB,
                svcC,
                svcD
        );
        serviceManager.registerStatusListener(this::recordServiceStatus);
        statusMap = new HashMap<>();
        updateStatus(statusMap, svcA.getName(), Service.Status.STATUS_UNKNOWN);
        updateStatus(statusMap, svcB.getName(), Service.Status.STATUS_UNKNOWN);
        updateStatus(statusMap, svcC.getName(), Service.Status.STATUS_UNKNOWN);
        updateStatus(statusMap, svcD.getName(), Service.Status.STATUS_UNKNOWN);
    }

}
