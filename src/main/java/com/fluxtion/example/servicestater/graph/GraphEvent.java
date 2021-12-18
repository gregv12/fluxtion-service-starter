package com.fluxtion.example.servicestater.graph;

import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.event.Event;
import lombok.ToString;

/**
 * A collection of events that are used within the graph
 */
public interface GraphEvent {

    /**
     * A base event class that provides filtering functionality, allows routing an event to a specifiic service controller
     * The filter should be the name of the service.
     */
    class FilteredGraphEvent implements Named, Event{
        private final String name;

        public FilteredGraphEvent(String name) {
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
    class RequestServiceStart extends FilteredGraphEvent{
        public RequestServiceStart(String name) {
            super(name);
        }
    }

    @ToString(callSuper = true)
    class RequestServiceStop extends FilteredGraphEvent{
        public RequestServiceStop(String name) {
            super(name);
        }
    }

    @ToString(callSuper = true)
    class NotifyServiceStarted extends FilteredGraphEvent{
        public NotifyServiceStarted(String name) {
            super(name);
        }
    }

    @ToString(callSuper = true)
    class NotifyServiceStopped extends FilteredGraphEvent{
        public NotifyServiceStopped(String name) {
            super(name);
        }
    }

    @ToString
    class PublishStartTask{}

    @ToString
    class PublishStopTask{}

    @ToString
    class PublishStatus{}

    @ToString
    class RequestStartAll{}

    @ToString
    class RequestStopAll{}
}
