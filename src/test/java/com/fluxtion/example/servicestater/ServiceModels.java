package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.helpers.CliTestClient;

import java.util.ArrayList;
import java.util.List;

public interface ServiceModels {

    String HANDLER_A = "handlerA";
    String HANDLER_B = "handlerB";
    String HANDLER_C = "handlerC";
    String AGG_AB = "aggAB";
    String CALC_C = "calcC";
    String PERSISTER = "persister";

    static ServiceManager buildModelA(boolean addAuditLog, boolean compiled) {
        Service handlerA = new Service(HANDLER_A);
        Service handlerB = new Service(HANDLER_B);
        Service handlerC = new Service(HANDLER_C);
        Service aggAB = new Service(AGG_AB, CliTestClient::notifyStartedAggAB, null, handlerA, handlerB);
        Service calcC = new Service(CALC_C, handlerC);
        Service persister = new Service(PERSISTER, CliTestClient::notifyStartedPersister, null, aggAB, calcC);
        //build and register outputs
        return new ServiceManager()
                .addAuditLog(addAuditLog)
                .compiled(compiled)
                .buildServiceController(persister, aggAB, calcC, handlerA, handlerB, handlerC);
    }

    static List<StatusForService> allWithStatus(Service.Status status){
        List<StatusForService> statusList = new ArrayList<>();
        statusList.add(new StatusForService(AGG_AB, status));
        statusList.add(new StatusForService(CALC_C, status));
        statusList.add(new StatusForService(HANDLER_A, status));
        statusList.add(new StatusForService(HANDLER_B, status));
        statusList.add(new StatusForService(HANDLER_C, status));
        statusList.add(new StatusForService(PERSISTER, status));
        return statusList;
    }

    static List<StatusForService> allUnknownStatus(){
        return allWithStatus(Service.Status.STATUS_UNKNOWN);
    }

    static List<StatusForService> allStartedStatus(){
        return allWithStatus(Service.Status.STARTED);
    }

}
