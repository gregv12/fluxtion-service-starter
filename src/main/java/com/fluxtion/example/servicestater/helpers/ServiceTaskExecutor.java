package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.example.servicestater.graph.TaskWrapper;
import lombok.extern.java.Log;

import java.util.List;
import java.util.function.Consumer;

@Log
public class ServiceTaskExecutor implements Consumer<List<TaskWrapper>> {

    @Override
    public void accept(List<TaskWrapper> command) {
        if(!command.isEmpty()){
            command.forEach(t ->{
                log.info("executing " + t.toString());
                t.getTask().run();
            });
        }
    }
}
