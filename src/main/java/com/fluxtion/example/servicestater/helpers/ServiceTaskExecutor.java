package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.example.servicestater.graph.TaskWrapper;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
@ToString
public class ServiceTaskExecutor implements Consumer<List<TaskWrapper>> {

    public static final long TIMEOUT_SECONDS = 10L;
    private final ExecutorService executorService;
    private static final LongAdder COUNT = new LongAdder();
    private boolean waitForTasks = false;

    public ServiceTaskExecutor() {
        executorService = Executors.newCachedThreadPool(this::namedThreadFactory);
    }

    public void shutDown(){
        executorService.shutdown();
    }

    @Override
    @SneakyThrows
    public void accept(List<TaskWrapper> tasks) {
        if(waitForTasks){
            try {
                log.debug("scheduling {} tasks for execution", tasks.size());
                List<Future<TaskWrapper.TaskExecutionResult>> results = executorService.invokeAll(tasks);//, TIMEOUT_SECONDS, TimeUnit.SECONDS);
                log.debug("completed all scheduled tasks");
                if(results.stream().anyMatch(Predicate.not(Future::isDone))){
                    log.warn("some tasks did not execute");
                }
            } catch (InterruptedException e) {
                log.warn("task execution interrupted");
            }
        }else{
            executorService.submit(() -> {
                if (!tasks.isEmpty()) {
                    tasks.forEach(t -> {
                        log.debug("executing " + t.toString());
                        t.getTask().run();
                    });
                }
            });
        }
    }

    private Thread namedThreadFactory(Runnable runnable) {
        Thread thread = new Thread(runnable, "taskExecutor-" + COUNT.intValue());
        COUNT.increment();
        return thread;
    }

    public void waitForTasksToComplete(boolean waitForTasks) {
        this.waitForTasks = waitForTasks;
    }
}
