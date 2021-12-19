package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.example.servicestater.ServiceStatusRecord;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@ToString
public class PublishServiceStatusRecordToLog implements Consumer<List<ServiceStatusRecord>> {

    @Override
    public void accept(List<ServiceStatusRecord> status) {
        log.info("Current status:\n" + status.stream()
                .map(Objects::toString)
                .collect(Collectors.joining("\n")));
    }
}
