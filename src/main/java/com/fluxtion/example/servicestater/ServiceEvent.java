package com.fluxtion.example.servicestater;

import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.event.Event;
import lombok.ToString;
import lombok.Value;

import java.util.List;
import java.util.function.Consumer;

public interface ServiceEvent extends Named, Event {

    static StatusUpdate newStartedUpdate(String name){
        return new StatusUpdate(ServiceStatus.STARTED, name);
    }

    static StatusUpdate newStoppedUpdate(String name){
        return new StatusUpdate(ServiceStatus.STOPPED, name);
    }

    static StatusUpdate newStatusUnknownUpdate(String name){
        return new StatusUpdate(ServiceStatus.STATUS_UNKNOWN, name);
    }

    static StatusUpdate newStartingUpdate(String name){
        return new StatusUpdate(ServiceStatus.STARTING, name);
    }

    static StatusUpdate newStoppingUpdate(String name){
        return new StatusUpdate(ServiceStatus.STOPPING, name);
    }

    class Command implements ServiceEvent {
        private final String name;

        public Command(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String filterString() {
            return getName();
        }

        @Override
        public String toString() {
            return "name='" + name + '\'' ;
        }
    }

    @ToString(callSuper = true)
    class StartSingleService extends Command{

        public StartSingleService(String name) {
            super(name);
        }
    }

    @ToString(callSuper = true)
    class StopSingleService extends Command{
        public StopSingleService(String name) {
            super(name);
        }
    }

    @ToString(callSuper = true)
    class StatusQuery  extends Command{
        public StatusQuery(String name) {
            super(name);
        }
    }

    @ToString(callSuper = true)
    class MakeUnavailable  extends Command{
        public MakeUnavailable(String name) {
            super(name);
        }
    }

    @Value
    class StatusUpdate implements ServiceEvent {
        ServiceStatus status;
        String name;

        @Override
        public String filterString() {
            return getName();
        }
    }

    @Value
    class RegisterCommandProcessor {
        Consumer<List<Command>> consumer;
    }

    @Value
    class RegisterStatusListener{
        Consumer<List<String>> statusListener;
    }

    @ToString
    class PublishStatus{}

    @ToString
    class StartAllServices{}

    @ToString
    class StopAllServices{}
}
