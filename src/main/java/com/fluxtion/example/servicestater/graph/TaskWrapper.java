package com.fluxtion.example.servicestater.graph;

import lombok.Value;

@Value
public class TaskWrapper {
    String serviceName;
    Runnable task;

    @Override
    public String toString() {
        return "TaskWrapper{" +
                "serviceName='" + serviceName + '\'' +
                '}';
    }
}
