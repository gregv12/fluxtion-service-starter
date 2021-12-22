package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.graph.TaskWrapperPublisher;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Encapsulates a task that a service has provided during registration.
 */
@Value
@Slf4j
public class TaskWrapper implements Callable<TaskWrapper.TaskExecutionResult> {

    /**
     * Processes {@link TaskWrapper} published by the {@link ServiceManager}
     */
    public interface TaskExecutor extends Consumer<List<TaskWrapper>>, AutoCloseable{}

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
            log.info("executing {}", this);
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
