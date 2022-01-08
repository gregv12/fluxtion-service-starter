package com.fluxtion.example.servicestater.graph;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fluxtion.example.servicestater.Service.Status;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
class BaseServiceStarterTest {

    protected boolean ADD_AUDIT_LOG = false;
    protected boolean COMPILED = false;
    protected final List<ServiceStatusRecord> statusList = new ArrayList<>();

    @BeforeEach
    public void beforeTest() {
        ADD_AUDIT_LOG = false;
        COMPILED = false;
    }

    protected void checkStatusMatch(List<ServiceStatusRecord> statusMap) {
        assertThat(statusList, Matchers.containsInAnyOrder(statusMap.toArray()));
    }

    protected void checkStatusMatch(Map<String, ServiceStatusRecord> statusMap) {
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
