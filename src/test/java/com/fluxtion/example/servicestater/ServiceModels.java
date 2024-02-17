package com.fluxtion.example.servicestater;

import com.fluxtion.compiler.generation.OutputRegistry;
import com.fluxtion.example.servicestater.graph.FluxtionServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ServiceModels {


    /**
     *
     *
     *
     * +-------------+                      +------------+              +-----------+
     * |             |                      |            |              |           |
     * |  handler_c  |                      | handler_a  |              | handler_b |
     * +---+---------+                      +----+-------+              +-----+-----+
     *     |                                     |                            |
     *     |    +---------+                      |                            |
     *     |    |         |                      |        +-------+           |
     *     +----+ calc_c  |                      +--------+agg_AB +-----------+
     *          +----+----+                               +---+---+
     *               |                                        |
     *               |                                        |
     *               |                +-----------+           |
     *               |                |           |           |
     *               +----------------+ persister +-----------+
     *                                |           |
     *                                +-----------+
     *
     *
     *
     *
     */

    String HANDLER_A = "handlerA";
    String HANDLER_B = "handlerB";
    String HANDLER_C = "handlerC";
    String AGG_AB = "aggAB";
    String CALC_C = "calcC";
    String PERSISTER = "persister";

    static FluxtionServiceManager buildModelA(boolean addAuditLog, boolean compiled) {
        Service handlerA = Service.builder(HANDLER_A).wrappedInstance("wrapped:" + HANDLER_A).build();
        Service handlerB = Service.builder(HANDLER_B).build();
        Service handlerC = Service.builder(HANDLER_C).build();
        Service aggAB = Service.builder(AGG_AB)
                .serviceListThatRequireMe(List.of(handlerA, handlerB))
                .build();
        Service calcC = Service.builder(CALC_C)
                .serviceListThatRequireMe(List.of(handlerC))
                .build();
        Service persister = Service.builder(PERSISTER)
                .serviceListThatRequireMe(List.of(aggAB, calcC))
                .build();
        //build and register outputs
        return new FluxtionServiceManager()
                .addAuditLog(addAuditLog)
                .compiled(compiled)
                .buildServiceController(persister, aggAB, calcC, handlerA, handlerB, handlerC);
    }

    static FluxtionServiceManager buildModelA(String className, boolean addAuditLog) {
        Service handlerA = Service.builder(HANDLER_A).wrappedInstance("wrapped:" + HANDLER_A).build();
        Service handlerB = Service.builder(HANDLER_B).build();
        Service handlerC = Service.builder(HANDLER_C).build();
        Service aggAB = Service.builder(AGG_AB)
                .serviceListThatRequireMe(List.of(handlerA, handlerB))
                .build();
        Service calcC = Service.builder(CALC_C)
                .serviceListThatRequireMe(List.of(handlerC))
                .build();
        Service persister = Service.builder(PERSISTER)
                .serviceListThatRequireMe(List.of(aggAB, calcC))
                .build();
        //build and register outputs
        return new FluxtionServiceManager()
                .addAuditLog(addAuditLog)
                .buildServiceControllerAot(
                        OutputRegistry.JAVA_TESTGEN_DIR,
                        className,
                        "com.fluxtion.example.servicestater.testgenerated", persister, aggAB, calcC, handlerA, handlerB, handlerC);
    }

    static Map<String, ServiceStatusRecord> mapWithStatus(Service.Status status) {
        return allWithStatus(status).stream()
                .collect(Collectors.toMap(ServiceStatusRecord::getServiceName, Function.identity()));
    }

    static List<ServiceStatusRecord> allWithStatus(Service.Status status) {
        List<ServiceStatusRecord> statusList = new ArrayList<>();
        statusList.add(new ServiceStatusRecord(AGG_AB, status));
        statusList.add(new ServiceStatusRecord(CALC_C, status));
        statusList.add(new ServiceStatusRecord(HANDLER_A, status));
        statusList.add(new ServiceStatusRecord(HANDLER_B, status));
        statusList.add(new ServiceStatusRecord(HANDLER_C, status));
        statusList.add(new ServiceStatusRecord(PERSISTER, status));
        return statusList;
    }

    static List<ServiceStatusRecord> allUnknownStatus() {
        return allWithStatus(Service.Status.STATUS_UNKNOWN);
    }

    static List<ServiceStatusRecord> allStartedStatus() {
        return allWithStatus(Service.Status.STARTED);
    }

}
