package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.helpers.ServiceTaskExecutor;
import lombok.extern.java.Log;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Log
class ServiceManagerTest {

    private final List<StatusForService> statusList = new ArrayList<>();

    @Test
    void buildSystemController() {
        ServiceManager serviceManager = ServiceModels.buildModelA(false, false);
        ServiceTaskExecutor serviceTaskExecutor = new ServiceTaskExecutor();
        serviceManager.registerStatusListener(this::recordServiceStatus);
        //
        assertEquals(6, statusList.size());
        assertThat(statusList, Matchers.containsInAnyOrder(ServiceModels.allUnknownStatus().toArray()));
    }


    public void recordServiceStatus(List<StatusForService> statusUpdate) {
        statusList.clear();
        statusList.addAll(statusUpdate);
//        log.info("Current status:\n" +
//                statusUpdate.stream()
//                        .map(Objects::toString)
//                        .collect(Collectors.joining("\n"))
//        );
    }
}
