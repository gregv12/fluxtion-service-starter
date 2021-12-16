package com.fluxtion.example.servicestater;

import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.annotations.AfterEvent;
import com.fluxtion.runtim.annotations.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CommandPublisher implements Named {

    private Consumer<List<ServiceEvent.Command>> commandPublisher = (command -> {});
    private final List<ServiceEvent.Command> commandList = new ArrayList<>();

    @EventHandler(propagate = false)
    public void registerCommandProcessor(ServiceEvent.RegisterCommandProcessor registerCommandProcessor) {
        this.commandPublisher = registerCommandProcessor.getConsumer();
    }

    public void publishCommand(ServiceEvent.Command command){
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
