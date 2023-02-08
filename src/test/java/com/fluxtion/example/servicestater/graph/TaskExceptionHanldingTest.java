package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.Service.ServiceBuilder;
import com.fluxtion.example.servicestater.Service.Status;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class TaskExceptionHanldingTest {

    private static boolean trace = true;

    private static ServiceBuilder simpleService(String name, boolean throwException) {
        return Service.builder(name)
                .startTask(() -> {
                    if (trace) {
                        System.out.println("starting:" + name);
                    }
                    if (throwException) {
                        throw new RuntimeException("starting:" + name);
                    }
                })
                .stopTask(() -> {
                    if (trace) {
                        System.out.println("stopping:" + name);
                    }
                    if (throwException) {
                        throw new RuntimeException("stopping:" + name);
                    }
                });
    }

    @Test
    public void rethrowException() {
        List<ServiceStatusRecord> statusList = new ArrayList<>();
        Service root = simpleService("root-1", true).build();
        Service a1 = simpleService("A1", false).requiredServices(root).build();

        ServiceManager serviceManager = ServiceManager.build(root, a1);
        serviceManager.registerStatusListener(c -> c.forEach(statusList::add));
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            serviceManager.startAllServices();
        });

        Assertions.assertEquals("starting:root-1", thrown.getMessage());
    }

    @Test
    public void swallowException() {
        List<ServiceStatusRecord> statusList = new ArrayList<>();
        Service root = simpleService("root-1", true).build();
        Service a1 = simpleService("A1", false).requiredServices(root).build();

        ServiceManager serviceManager = ServiceManager.build(root, a1);
        serviceManager.failFastOnTaskException(false);
        serviceManager.registerStatusListener(c -> c.forEach(statusList::add));
        serviceManager.triggerNotificationOnSuccessfulTaskExecution(true);
        serviceManager.startAllServices();

        //TEST
        statusList.clear();
        serviceManager.publishSystemStatus();
        assertThat(statusList, Matchers.containsInAnyOrder(
                new ServiceStatusRecord("root-1", Status.STARTING),
                new ServiceStatusRecord("A1", Status.WAITING_FOR_PARENTS_TO_START)));
    }
}
