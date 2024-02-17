package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class CascadeOnSuccessfulTaskExecutionTest extends BaseServiceStarterTest {

    protected Service svcA;
    protected Service svcB;
    protected Service svcC;
    protected Service svcD;
    protected ServiceManager serviceManager;
    protected HashMap<String, ServiceStatusRecord> statusMap;

    @Test
    public void startAllService(){
        svcA = Service.builder("A")
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcB = Service.builder("B").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcC = Service.builder("C").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcD = Service.builder("D").servicesThatRequireMe(svcB, svcC)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        buildGraph();
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);
        ADD_AUDIT_LOG = true;

        serviceManager.startAllServices();
        updateStatus(statusMap, svcA.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);
    }

    @Test
    public void startAllSingleService(){
        svcA = Service.builder("A")
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcB = Service.builder("B").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcC = Service.builder("C").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcD = Service.builder("D").servicesThatRequireMe(svcB, svcC)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        buildGraph();
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);
        ADD_AUDIT_LOG = true;

        serviceManager.startService(svcA.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);
    }

    @Test
    public void startSomeSingleService_NoStartTaskForC(){
        svcA = Service.builder("A")
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcB = Service.builder("B").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcC = Service.builder("C").servicesThatRequireMe(svcA)
                .build();
        svcD = Service.builder("D").servicesThatRequireMe(svcB, svcC)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        buildGraph();
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);
        ADD_AUDIT_LOG = true;

        serviceManager.startService(svcA.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.WAITING_FOR_PARENTS_TO_START);
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTING);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);
    }

    @Test
    public void startSomeSingleService_ExceptionInStartTaskForB(){
        svcA = Service.builder("A")
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcB = Service.builder("B").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::fail)
                .build();
        svcC = Service.builder("C").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcD = Service.builder("D").servicesThatRequireMe(svcB, svcC)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        buildGraph();
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);
        ADD_AUDIT_LOG = true;

        serviceManager.failFastOnTaskException(false);
        serviceManager.startService(svcA.getName());
        updateStatus(statusMap, svcA.getName(), Service.Status.WAITING_FOR_PARENTS_TO_START);
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTING);
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);
    }

    @Test
    public void startAllServiceThenStopAll(){
        svcA = Service.builder("A")
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcB = Service.builder("B").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcC = Service.builder("C").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcD = Service.builder("D").servicesThatRequireMe(svcB, svcC)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        buildGraph();
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);
        ADD_AUDIT_LOG = true;

        serviceManager.startAllServices();
        updateStatus(statusMap, svcA.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);

        serviceManager.stopAllServices();
        updateStatus(statusMap, svcA.getName(), Service.Status.STOPPED);
        updateStatus(statusMap, svcB.getName(), Service.Status.STOPPED);
        updateStatus(statusMap, svcC.getName(), Service.Status.STOPPED);
        updateStatus(statusMap, svcD.getName(), Service.Status.STOPPED);
        checkStatusMatch(statusMap);
    }

    @Test
    public void startAllServiceThenStopAllWithExceptionInC_StopTask(){
        svcA = Service.builder("A")
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcB = Service.builder("B").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcC = Service.builder("C").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::fail)
                .build();
        svcD = Service.builder("D").servicesThatRequireMe(svcB, svcC)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        buildGraph();
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);
        ADD_AUDIT_LOG = true;

        serviceManager.startAllServices();
        updateStatus(statusMap, svcA.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);

        serviceManager.failFastOnTaskException(false);
        serviceManager.stopAllServices();
        updateStatus(statusMap, svcA.getName(), Service.Status.STOPPED);
        updateStatus(statusMap, svcB.getName(), Service.Status.STOPPED);
        updateStatus(statusMap, svcC.getName(), Service.Status.STOPPING);
        updateStatus(statusMap, svcD.getName(), Service.Status.WAITING_FOR_PARENTS_TO_STOP);
        checkStatusMatch(statusMap);
    }

    @Test
    public void startAllServiceThenStopAllWithExceptionInC_StopTask_AndIgnoreException(){
        svcA = Service.builder("A")
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcB = Service.builder("B").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        svcC = Service.builder("C").servicesThatRequireMe(svcA)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::fail)
                .build();
        svcD = Service.builder("D").servicesThatRequireMe(svcB, svcC)
                .startTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .stopTask(CascadeOnSuccessfulTaskExecutionTest::success)
                .build();
        buildGraph();
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);
        ADD_AUDIT_LOG = true;
        auditOn(true);

        serviceManager.startAllServices();
        updateStatus(statusMap, svcA.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcB.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcC.getName(), Service.Status.STARTED);
        updateStatus(statusMap, svcD.getName(), Service.Status.STARTED);
        checkStatusMatch(statusMap);

        serviceManager.triggerNotificationAfterTaskExecution(true);
        serviceManager.stopAllServices();
        updateStatus(statusMap, svcA.getName(), Service.Status.STOPPED);
        updateStatus(statusMap, svcB.getName(), Service.Status.STOPPED);
        updateStatus(statusMap, svcC.getName(), Service.Status.STOPPED);
        updateStatus(statusMap, svcD.getName(), Service.Status.STOPPED);
        checkStatusMatch(statusMap);
    }

    protected void buildGraph(){


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

    public static void success(){}

    public static void fail(){
        throw new RuntimeException("failed execution");
    }
}
