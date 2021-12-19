package com.fluxtion.example.servicestater;

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
        Service handlerA = new Service(HANDLER_A);
        Service handlerB = new Service(HANDLER_B);
        Service handlerC = new Service(HANDLER_C);
        Service aggAB = new Service(AGG_AB, handlerA, handlerB);
        Service calcC = new Service(CALC_C, handlerC);
        Service persister = new Service(PERSISTER, aggAB, calcC);
        //build and register outputs
        return new FluxtionServiceManager()
                .addAuditLog(addAuditLog)
                .compiled(compiled)
                .buildServiceController(persister, aggAB, calcC, handlerA, handlerB, handlerC);
    }

    static Map<String, StatusForService> mapWithStatus(Service.Status status) {
        return allWithStatus(status).stream()
                .collect(Collectors.toMap(StatusForService::getServiceName, Function.identity()));
    }

    static List<StatusForService> allWithStatus(Service.Status status) {
        List<StatusForService> statusList = new ArrayList<>();
        statusList.add(new StatusForService(AGG_AB, status));
        statusList.add(new StatusForService(CALC_C, status));
        statusList.add(new StatusForService(HANDLER_A, status));
        statusList.add(new StatusForService(HANDLER_B, status));
        statusList.add(new StatusForService(HANDLER_C, status));
        statusList.add(new StatusForService(PERSISTER, status));
        return statusList;
    }

    static List<StatusForService> allUnknownStatus() {
        return allWithStatus(Service.Status.STATUS_UNKNOWN);
    }

    static List<StatusForService> allStartedStatus() {
        return allWithStatus(Service.Status.STARTED);
    }

}