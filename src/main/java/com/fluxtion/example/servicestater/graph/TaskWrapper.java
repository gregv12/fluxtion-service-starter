package com.fluxtion.example.servicestater.graph;

import lombok.Value;

/**
 * Encapsulates a task that a service has provided during registration. Task are published by the {@link TaskWrapperPublisher}
 */
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
