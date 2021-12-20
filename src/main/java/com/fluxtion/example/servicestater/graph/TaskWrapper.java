package com.fluxtion.example.servicestater.graph;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * Encapsulates a task that a service has provided during registration. Task are published by the {@link TaskWrapperPublisher}
 */
@Value
@Slf4j
public class TaskWrapper implements Callable<TaskWrapper.TaskExecutionResult> {
    String serviceName;
    boolean startTask;
    Runnable task;

    @Override
    public String toString() {
        return "TaskWrapper{" +
                "serviceName='" + serviceName + '\'' +
                ", startTask='" + startTask + '\'' +
                '}';
    }

    @Override
    public TaskExecutionResult call() {
        TaskExecutionResult result;
        try{
            log.debug("executing {}", this);
            task.run();
            result = new TaskExecutionResult(true, isStartTask(), getServiceName());
        }catch (Exception e){
            log.warn("problem executing task", e);
            result = new TaskExecutionResult(false,  isStartTask(), getServiceName());
        }
        log.debug("completed executing: {}", result);
        return result;
    }

    @Value
    public static class TaskExecutionResult {
        boolean success;
        boolean startTask;
        String serviceName;
    }
}
