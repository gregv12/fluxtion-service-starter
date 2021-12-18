package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.example.servicestater.graph.TaskWrapper;
import lombok.extern.java.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Log
public class ServiceTaskExecutor implements Consumer<List<TaskWrapper>> {

    private final ExecutorService executorService;

    public ServiceTaskExecutor() {
        executorService = Executors.newCachedThreadPool();
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
}
