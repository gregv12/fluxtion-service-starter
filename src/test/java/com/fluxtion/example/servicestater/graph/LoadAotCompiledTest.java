package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class LoadAotCompiledTest extends BaseServiceStarterTest {
    private static boolean Astarted = false;
    private static boolean Bstarted = false;

    @BeforeEach
    public void init(){
        Astarted = false;
        Bstarted = false;
    }

    private static void buildModel(){
        Service a = Service.builder("A").startTask(LoadAotCompiledTest::startA).build();
        Service b = Service.builder("B").requiredServices(a).startTask(LoadAotCompiledTest::startB).build();
        ServiceManager.compiledServiceManager(a, b);
    }

    @Test
    public void testLoad(){
//        buildModel();
        ServiceManager serviceManager = ServiceManager.fromProcessor(new Processor());
        Assertions.assertFalse(Astarted);
        Assertions.assertFalse(Bstarted);

        serviceManager.registerStatusListener(this::recordServiceStatus);
        HashMap<String, ServiceStatusRecord> statusMap  = new HashMap<>();
        updateStatus(statusMap, "A", Service.Status.STATUS_UNKNOWN);
        updateStatus(statusMap, "B", Service.Status.STATUS_UNKNOWN);
        checkStatusMatch(statusMap);

        serviceManager.startService("B");
        updateStatus(statusMap, "A", Service.Status.STARTING);
        updateStatus(statusMap, "B", Service.Status.WAITING_FOR_PARENTS_TO_START);

        serviceManager.serviceStarted("A");
        updateStatus(statusMap, "A", Service.Status.STARTED);
        updateStatus(statusMap, "B", Service.Status.STARTING);

    }


    public static void startA(){
        Astarted = true;
    }

    public static void startB(){
        Bstarted = true;
    }
}
