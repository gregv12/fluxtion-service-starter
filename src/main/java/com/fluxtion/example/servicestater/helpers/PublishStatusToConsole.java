package com.fluxtion.example.servicestater.helpers;

import lombok.extern.java.Log;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Log
public class PublishStatusToConsole implements Consumer<List<String>> {

    @Override
    public void accept(List<String> status) {
        log.info("Current status:\n" + status.stream().collect(Collectors.joining("\n")));
    }
}
