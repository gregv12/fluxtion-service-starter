package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.example.servicestater.StatusForService;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Log
public class PublishStatusToConsole implements Consumer<List<StatusForService>> {

    @Override
    public void accept(List<StatusForService> status) {
        log.info("Current status:\n" + status.stream().map(Objects::toString).collect(Collectors.joining("\n")));
    }
}
