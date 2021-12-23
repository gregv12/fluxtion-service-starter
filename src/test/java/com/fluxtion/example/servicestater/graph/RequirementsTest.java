package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequirementsTest extends FluxtionServiceManagerModelATest{

    public static final String SVC_INPUT = "svcInput";
    public static final String SVC_ROOT = "svcRoot";
    private ServiceManager serviceManager;

    @Test
    public void testRequirementsSetting(){
        Service svcRoot = Service.builder(SVC_ROOT)
                .build();
        Service svcInput = Service.builder(SVC_INPUT)
                .requiredServices(List.of(svcRoot))
                .build();
        serviceManager = ServiceManager.interpretedServiceManager(svcRoot, svcInput);
        validateStartInOrder(svcInput, svcRoot);
    }


    @Test
    public void testDependentSetting(){
        Service svcInput = Service.builder(SVC_INPUT)
                .build();
        Service svcRoot = Service.builder(SVC_ROOT)
                .servicesThatRequireMe(List.of(svcInput))
                .build();

        serviceManager = ServiceManager.interpretedServiceManager(svcRoot, svcInput);
        validateStartInOrder(svcInput, svcRoot);
    }

    private void validateStartInOrder(Service svcInput, Service svcRoot) {
        serviceManager.registerStatusListener(this::recordServiceStatus);
        Map<String, ServiceStatusRecord> map = new HashMap<>();
        map.put(SVC_INPUT, new ServiceStatusRecord(SVC_INPUT, Service.Status.STATUS_UNKNOWN));
        map.put(SVC_ROOT, new ServiceStatusRecord(SVC_ROOT, Service.Status.STATUS_UNKNOWN));
        checkStatusMatch(map);

        //start
        serviceManager.startService(SVC_INPUT);
        map.put(SVC_ROOT, new ServiceStatusRecord(SVC_ROOT, Service.Status.STARTING));
        map.put(SVC_INPUT, new ServiceStatusRecord(SVC_INPUT, Service.Status.WAITING_FOR_PARENTS_TO_START));
        checkStatusMatch(map);

        //started SVC_ROOT
        serviceManager.serviceStarted(SVC_ROOT);
        map.put(SVC_ROOT, new ServiceStatusRecord(SVC_ROOT, Service.Status.STARTED));
        map.put(SVC_INPUT, new ServiceStatusRecord(SVC_INPUT, Service.Status.STARTING));
        checkStatusMatch(map);

        //started SVC_INPUT
        serviceManager.serviceStarted(SVC_INPUT);
        map.put(SVC_ROOT, new ServiceStatusRecord(SVC_ROOT, Service.Status.STARTED));
        map.put(SVC_INPUT, new ServiceStatusRecord(SVC_INPUT, Service.Status.STARTED));
        checkStatusMatch(map);
    }
}