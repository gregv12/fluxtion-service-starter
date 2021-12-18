package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.Service.Status;
import lombok.extern.java.Log;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fluxtion.example.servicestater.ServiceModels.mapWithStatus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Log
class ServiceManagerModelATest {

    /**
     * <pre>
     *
     * Tree view of model A
     *
     * +-------------+                      +------------+              +-----------+      |
     * |             |                      |            |              |           |      |
     * |  handler_c  |                      | handler_a  |              | handler_b |      |
     * +---+---------+                      +----+-------+              +-----+-----+      |
     *     |                                     |                            |            |
     *     |    +---------+                      |        +-------+           |            |   DIRECTION OF
     *     |    |         |                      |        |       |           |            |   EVENT FLOW
     *     +----+ calc_c  |                      +--------+agg_AB +-----------+            |
     *          +----+----+                               +---+---+                        |
     *               |                                        |                            |
     *               |                                        |                            |
     *               |                +-----------+           |                            |
     *               |                |           |           |                            |
     *               +----------------+ persister +-----------+                            |
     *                                |           |                                        |
     *                                +-----------+                                        v
     *
     *
     * </pre>
     * drawn with - https://asciiflow.com/#/
     */


    private boolean ADD_AUDIT_LOG = false;
    private boolean COMPILED = false;
    private final List<StatusForService> statusList = new ArrayList<>();

    @BeforeEach
    public void beforeTest() {
        ADD_AUDIT_LOG = false;
        COMPILED = false;
    }

    @Test
    void buildSystemController() {
        var serviceManager = ServiceModels.buildModelA(ADD_AUDIT_LOG, COMPILED);
        serviceManager.registerStatusListener(this::recordServiceStatus);
        assertEquals(6, statusList.size());
        checkStatusMatch(ServiceModels.allUnknownStatus());
    }

    @Test
    void startingAggAB() {
        var serviceManager = ServiceModels.buildModelA(ADD_AUDIT_LOG, COMPILED);
        serviceManager.registerStatusListener(this::recordServiceStatus);
        serviceManager.startService(ServiceModels.AGG_AB);
        var statusMap = mapWithStatus(Status.STATUS_UNKNOWN);
        updateStatus(statusMap, ServiceModels.AGG_AB, Status.WAITING_FOR_PARENTS_TO_START);
        updateStatus(statusMap, ServiceModels.PERSISTER, Status.STARTING);
        checkStatusMatch(statusMap);
    }

    @Test
    void startingAggABThenNotifyPersisterStart() {
//        ADD_AUDIT_LOG = true;
//        COMPILED = true;
        var serviceManager = startAService(ServiceModels.AGG_AB);
        var statusMap = mapWithStatus(Status.STATUS_UNKNOWN);
        serviceManager.serviceStartedNotification(ServiceModels.PERSISTER);
        updateStatus(statusMap, ServiceModels.AGG_AB, Status.STARTING);
        updateStatus(statusMap, ServiceModels.PERSISTER, Status.STARTED);
        checkStatusMatch(statusMap);
    }

    private ServiceManager startAService(String serviceName) {
        ServiceManager serviceManager = ServiceModels.buildModelA(ADD_AUDIT_LOG, COMPILED);
        serviceManager.registerStatusListener(this::recordServiceStatus);
        serviceManager.startService(serviceName);
        return serviceManager;
    }

    private void checkStatusMatch(List<StatusForService> statusMap) {
        assertThat(statusList, Matchers.containsInAnyOrder(statusMap.toArray()));
    }

    private void checkStatusMatch(Map<String, StatusForService> statusMap) {
        assertThat(statusList, Matchers.containsInAnyOrder(statusMap.values().toArray()));
    }

    static void updateStatus(Map<String, StatusForService> statusMap, String serviceName, Status status) {
        statusMap.put(serviceName, new StatusForService(serviceName, status));
    }


    public void recordServiceStatus(List<StatusForService> statusUpdate) {
        statusList.clear();
        statusList.addAll(statusUpdate);
        if (ADD_AUDIT_LOG) {
            log.info("Current status:\n" +
                    statusUpdate.stream()
                            .map(Objects::toString)
                            .collect(Collectors.joining("\n"))
            );
        }
    }
}
