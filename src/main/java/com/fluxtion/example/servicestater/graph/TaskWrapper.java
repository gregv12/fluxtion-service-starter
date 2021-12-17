package com.fluxtion.example.servicestater.graph;

import lombok.Value;

@Value
public class TaskWrapper {
    String serviceName;
    boolean startTask;
    Runnable task;

    @Override
    public String toString() {
        return "TaskWrapper{" +
                "serviceName='" + serviceName + '\'' +
                "startTask='" + startTask + '\'' +
                '}';
    }
}
