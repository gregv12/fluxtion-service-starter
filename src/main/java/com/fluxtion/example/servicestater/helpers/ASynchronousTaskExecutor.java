package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.example.servicestater.TaskWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
public class ASynchronousTaskExecutor implements TaskWrapper.TaskExecutor {

    private final ExecutorService executorService;
    private static final LongAdder COUNT = new LongAdder();

    public ASynchronousTaskExecutor() {
        executorService = Executors.newCachedThreadPool(this::namedThreadFactory);
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    @Override
    public void accept(List<TaskWrapper> taskWrapper) {
        taskWrapper.forEach(executorService::submit);
    }

    private Thread namedThreadFactory(Runnable runnable) {
        Thread thread = new Thread(runnable, "taskExecutor-" + COUNT.intValue());
        COUNT.increment();
        return thread;
    }
}
