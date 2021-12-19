package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.example.servicestater.graph.TaskWrapper;
import lombok.ToString;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

@Slf4j
@ToString
public class ServiceTaskExecutor implements Consumer<List<TaskWrapper>> {

    private final ExecutorService executorService;
    private static final LongAdder COUNT = new LongAdder();

    public ServiceTaskExecutor() {
        executorService = Executors.newCachedThreadPool(this::namedThreadFactory);
    }

    public void shutDown(){
        executorService.shutdown();
    }

    @Override
    public void accept(List<TaskWrapper> command) {
        executorService.submit(() -> {
            if (!command.isEmpty()) {
                command.forEach(t -> {
                    log.info("executing " + t.toString());
                    t.getTask().run();
                });
            }
        });
    }

    private Thread namedThreadFactory(Runnable runnable) {
        Thread thread = new Thread(runnable, "taskExecutor-" + COUNT.intValue());
        COUNT.increment();
        return thread;
    }
}
