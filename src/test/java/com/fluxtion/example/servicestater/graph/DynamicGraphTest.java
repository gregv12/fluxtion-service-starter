package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.Service.ServiceBuilder;
import com.fluxtion.example.servicestater.Service.Status;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class DynamicGraphTest {

    private static boolean trace = false;
    private List<ServiceStatusRecord> statusList;
    private ServiceManager serviceManager;

    private static ServiceBuilder simpleService(String name) {
        return Service.builder(name)
                .startTask(() -> {
                    if (trace) {
                        System.out.println("starting:" + name);
                    }
                })
                .stopTask(() -> {
                    if (trace) {
                        System.out.println("stopping:" + name);
                    }
                });
    }

    @BeforeEach
    public void testSetup() {
        statusList = new ArrayList<>();
    }

    private void validateStatusList(ServiceStatusRecord... expectedStatus) {
        statusList.clear();
        serviceManager.publishSystemStatus();
        assertThat(statusList, Matchers.containsInAnyOrder(expectedStatus));
    }

    private void buildServiceManager(Service... serviceList) {
        serviceManager = ServiceManager.build(serviceList);
        serviceManager.registerStatusListener(c -> c.forEach(statusList::add));
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);
        serviceManager.startAllServices();
    }

    @Test
    public void addServiceAfterStart() {
        List<ServiceStatusRecord> statusList = new ArrayList<>();
        Service root = simpleService("root-1").build();
        Service a1 = simpleService("A1").requiredServices(root).build();

        ServiceManager serviceManager = ServiceManager.build(root, a1);
        serviceManager.registerStatusListener(c -> c.forEach(statusList::add));
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);
        serviceManager.startAllServices();

        //TEST
        statusList.clear();
        serviceManager.publishSystemStatus();
        assertThat(statusList, Matchers.containsInAnyOrder(
                new ServiceStatusRecord("root-1", Status.STARTED),
                new ServiceStatusRecord("A1", Status.STARTED)));


        //Rebuid graph with new service
        Service newService = simpleService("after-thefact").requiredServices(root).build();
        serviceManager.addService(newService);
        serviceManager.publishSystemStatus();
        //TEST
        statusList.clear();
        serviceManager.publishSystemStatus();
        assertThat(statusList, Matchers.containsInAnyOrder(
                new ServiceStatusRecord("after-thefact", Status.STATUS_UNKNOWN),
                new ServiceStatusRecord("root-1", Status.STARTED),
                new ServiceStatusRecord("A1", Status.STARTED)));


        serviceManager.startService(newService.getName());
        //TEST
        statusList.clear();
        serviceManager.publishSystemStatus();
        assertThat(statusList, Matchers.containsInAnyOrder(
                new ServiceStatusRecord("after-thefact", Status.STARTED),
                new ServiceStatusRecord("root-1", Status.STARTED),
                new ServiceStatusRecord("A1", Status.STARTED)));

        //Stop root
        serviceManager.stopService(root.getName());
        //TEST
        statusList.clear();
        serviceManager.publishSystemStatus();
        assertThat(statusList, Matchers.containsInAnyOrder(
                new ServiceStatusRecord("after-thefact", Status.STOPPED),
                new ServiceStatusRecord("root-1", Status.STOPPED),
                new ServiceStatusRecord("A1", Status.STOPPED)));
    }

    @Test
    public void addSameServiceMultipleTimesAfterStart() {
        List<ServiceStatusRecord> statusList = new ArrayList<>();
        Service root = simpleService("root-1").build();
        Service a1 = simpleService("A1").requiredServices(root).build();

        ServiceManager serviceManager = ServiceManager.build(root, a1);
        serviceManager.registerStatusListener(c -> c.forEach(statusList::add));
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);
        serviceManager.startAllServices();

        Service newService = simpleService("after-thefact").requiredServices(root).build();
        serviceManager.addService(newService);

        newService = simpleService("after-thefact").requiredServices(root).build();
        serviceManager.addService(newService);
    }

    @Test
    public void removeServiceAfterStart() {
        Service root = simpleService("root-1").build();
        Service a1 = simpleService("A1").requiredServices(root).build();
        Service remove1 = simpleService("remove1").requiredServices(root).build();

        buildServiceManager(root, a1, remove1);

        //TEST
        validateStatusList(
                new ServiceStatusRecord("root-1", Status.STARTED),
                new ServiceStatusRecord("A1", Status.STARTED),
                new ServiceStatusRecord("remove1", Status.STARTED)
        );

        //REMOVE and TEST
        serviceManager.removeService("remove1");
        validateStatusList(
                new ServiceStatusRecord("root-1", Status.STARTED),
                new ServiceStatusRecord("A1", Status.STARTED)
        );
    }

    @Test
    public void removeServiceStopDependencyAfterStart() {
        Service root = simpleService("root-1").build();
        Service a1 = simpleService("A1").requiredServices(root).build();
        Service remove1 = simpleService("remove1").requiredServices(root).build();
        Service dependsOnRemoved = simpleService("dependsOnRemoved").requiredServices(remove1).build();

        buildServiceManager(root, a1, remove1, dependsOnRemoved);

        //TEST
        validateStatusList(
                new ServiceStatusRecord("root-1", Status.STARTED),
                new ServiceStatusRecord("A1", Status.STARTED),
                new ServiceStatusRecord("dependsOnRemoved", Status.STARTED),
                new ServiceStatusRecord("remove1", Status.STARTED)
        );

        //REMOVE and TEST
        serviceManager.removeService("remove1");
        validateStatusList(
                new ServiceStatusRecord("root-1", Status.STARTED),
                new ServiceStatusRecord("A1", Status.STARTED),
                new ServiceStatusRecord("dependsOnRemoved", Status.STOPPED)
        );
    }

}
