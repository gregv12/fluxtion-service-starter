package com.fluxtion.example.servicestater.graph;

import com.fluxtion.compiler.Fluxtion;
import com.fluxtion.runtime.EventProcessorContext;
import com.fluxtion.runtime.annotations.Initialise;
import com.fluxtion.runtime.annotations.OnEventHandler;
import com.fluxtion.runtime.annotations.builder.Inject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MyScratch {

    public static void main(String[] args) {
        var eventProcessor = Fluxtion.compile(c -> c.addNode(new MyStringHandler()));
        eventProcessor.setContextParameterMap(Map.of(
                "started", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")),
                "key1", "value1"
        ));
        eventProcessor.init();
        eventProcessor.onEvent("key1");
    }

    public static class MyStringHandler {
        @Inject
        public EventProcessorContext context;
        public String in;

        @OnEventHandler
        public boolean stringKeyUpdated(String in) {
            System.out.println("mapping " + in + " -> '" + context.getContextProperty(in) + "'");
            return true;
        }

        @Initialise
        public void init() {
            System.out.println("started: '" + context.getContextProperty("started") + "'");
        }
    }
}