package com.fluxtion.example.servicestater.graph;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fluxtion.example.servicestater.Service.Status;
import com.fluxtion.example.servicestater.ServiceModels;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fluxtion.example.servicestater.ServiceModels.mapWithStatus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Slf4j
class FluxtionServiceManagerModelATest {

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
    private final List<ServiceStatusRecord> statusList = new ArrayList<>();

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
        serviceManager.serviceStarted(ServiceModels.PERSISTER);
        updateStatus(statusMap, ServiceModels.AGG_AB, Status.STARTING);
        updateStatus(statusMap, ServiceModels.PERSISTER, Status.STARTED);
        checkStatusMatch(statusMap);
    }

    private FluxtionServiceManager startAService(String serviceName) {
        FluxtionServiceManager fluxtionServiceManager = ServiceModels.buildModelA(ADD_AUDIT_LOG, COMPILED);
        fluxtionServiceManager.registerStatusListener(this::recordServiceStatus);
        fluxtionServiceManager.startService(serviceName);
        return fluxtionServiceManager;
    }

    private void checkStatusMatch(List<ServiceStatusRecord> statusMap) {
        assertThat(statusList, Matchers.containsInAnyOrder(statusMap.toArray()));
    }

    private void checkStatusMatch(Map<String, ServiceStatusRecord> statusMap) {
        assertThat(statusList, Matchers.containsInAnyOrder(statusMap.values().toArray()));
    }

    static void updateStatus(Map<String, ServiceStatusRecord> statusMap, String serviceName, Status status) {
        statusMap.put(serviceName, new ServiceStatusRecord(serviceName, status));
    }


    public void recordServiceStatus(List<ServiceStatusRecord> statusUpdate) {
        statusList.clear();
        statusList.addAll(statusUpdate);
        if (ADD_AUDIT_LOG) {
            logStatus(statusUpdate);
        }
    }

    public static void logStatus(List<ServiceStatusRecord> statusUpdate){
        log.info("Current status:\n" +
                statusUpdate.stream()
                        .map(Objects::toString)
                        .collect(Collectors.joining("\n"))
        );
    }

    public static void auditOn(boolean flag){
        Logger restClientLogger = (Logger) LoggerFactory.getLogger("fluxtion.eventLog");
        if(flag){
            restClientLogger.setLevel(Level.INFO);
        }else{
            restClientLogger.setLevel(Level.OFF);
        }
    }
}
