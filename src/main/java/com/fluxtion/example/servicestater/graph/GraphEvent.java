/*
 * Copyright (c) Greg Higgins 2021.
 *
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.gnu.org/licenses/agpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.runtime.event.Event;
import com.fluxtion.runtime.node.NamedNode;
import lombok.ToString;

/**
 * A collection of events that are used within the graph
 */
public interface GraphEvent {

    /**
     * A base event class that provides filtering functionality, allows routing of an event to a specific
     * {@link ServiceController} and method. The filter should be the name of the service.
     */
    class FilteredGraphEvent implements NamedNode, Event {
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
            return "name='" + name + '\'';
        }
    }

    @ToString(callSuper = true)
    class RequestServiceStart extends FilteredGraphEvent {
        public RequestServiceStart(String name) {
            super(name);
        }
    }

    @ToString(callSuper = true)
    class RequestServiceStop extends FilteredGraphEvent {
        public RequestServiceStop(String name) {
            super(name);
        }
    }

    @ToString(callSuper = true)
    class NotifyServiceStarted extends FilteredGraphEvent {
        public NotifyServiceStarted(String name) {
            super(name);
        }
    }

    @ToString(callSuper = true)
    class NotifyServiceStopped extends FilteredGraphEvent {
        public NotifyServiceStopped(String name) {
            super(name);
        }
    }

    @ToString
    class PublishStartTask {
    }

    @ToString
    class PublishStopTask {
    }

    @ToString
    class PublishStatus {
    }

    @ToString
    class RequestStartAll {
    }

    @ToString
    class RequestStopAll {
    }

    @ToString
    class RemoveService {
        private final String serviceName;

        public RemoveService(Service service){
            this(service.getName());
        }

        public RemoveService(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public boolean serviceMatch(ServiceController service){
            return service.getServiceName().equals(serviceName);
        }
    }
}
