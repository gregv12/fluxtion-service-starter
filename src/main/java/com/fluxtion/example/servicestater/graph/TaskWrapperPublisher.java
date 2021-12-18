package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.ServiceEvent;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.annotations.AfterEvent;
import com.fluxtion.runtim.annotations.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Publishes commands for a service that are for execution by client code, a client application registers a command executor
 * by calling {@link ServiceManager#registerTaskExecutor(Consumer)}
 *
 * The command list is of events that can be executed in parallel by the registered client.
 */
public class TaskWrapperPublisher implements Named {

    private Consumer<List<TaskWrapper>> commandPublisher = (command -> {});
    private final List<TaskWrapper> commandList = new ArrayList<>();

    @EventHandler(propagate = false)
    public void registerCommandProcessor(ServiceEvent.RegisterCommandProcessor registerCommandProcessor) {
        this.commandPublisher = registerCommandProcessor.getConsumer();
    }

    public void publishCommand(TaskWrapper command){
        commandList.add(command);
    }

    @AfterEvent
    public void publishCommands() {
        if(!commandList.isEmpty()){
            commandPublisher.accept(new ArrayList<>(commandList));
            commandList.clear();
        }
    }

    @Override
    public String getName() {
        return "commandPublisher";
    }
}
