package com.fluxtion.example.servicestater.helpers;

import lombok.extern.java.Log;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.fluxtion.example.servicestater.ServiceEvent.Command;

@Log
public class PublishCommandsToConsole implements Consumer<List<Command>> {

    @Override
    public void accept(List<Command> command) {
        if(!command.isEmpty()){
            log.info("Command list:\n" + command.stream().map(Objects::toString).collect(Collectors.joining("\n")));
        }
    }
}
