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
import com.fluxtion.example.servicestater.ServiceOrderRecord;
import com.fluxtion.example.servicestater.graph.GraphEvent.PublishStopTask;
import com.fluxtion.runtime.annotations.NoPropagateFunction;
import com.fluxtion.runtime.annotations.OnEventHandler;
import com.fluxtion.runtime.annotations.OnTrigger;

import java.util.function.Consumer;

import static com.fluxtion.example.servicestater.Service.Status.*;
import static com.fluxtion.example.servicestater.graph.FluxtionServiceManager.toStartServiceName;

/**
 * A controller for notifying services in the graph, based upon the topological order of the nodes in the graph. The
 * topological order is respect to event processing, the top node(s) of the tree with no parents is an event handler.
 * Events enter the graph at the event handler, and notifications pass down the graph.
 * <p>
 * <p>
 * Reacts to events:
 * <ul>
 *   <li></li>
 *   <li> </li>
 *   <li>Change notifications to parent {@link ServiceController} nodes</li>
 * </ul>
 */
public class ForwardPassServiceController extends ServiceController {
    public ForwardPassServiceController(String serviceName, TaskWrapperPublisher taskWrapperPublisher, ServiceStatusRecordCache serviceStatusRecordCache) {
        super(serviceName, toStartServiceName(serviceName), taskWrapperPublisher, serviceStatusRecordCache);
    }

    @OnEventHandler
    public boolean startAllServices(GraphEvent.RequestStartAll startAll){
        return startServiceRequest();
    }

    @OnEventHandler(filterVariable = "serviceName")
    public boolean startThisService(GraphEvent.RequestServiceStart startSingleService) {
        return startServiceRequest();
    }

    private boolean startServiceRequest() {
        boolean changed = false;
        Service.Status startStatus = getStatus();
        auditLog.info("nullStatus", startStatus == null);
        if(startStatus == null){
            startStatus = STATUS_UNKNOWN;
        }
        switch (startStatus) {
            case STATUS_UNKNOWN:
            case WAITING_FOR_PARENTS_TO_STOP:
            case STOPPING:
            case STOPPED:{
                setStatus(Service.Status.WAITING_FOR_PARENTS_TO_START);
                changed = true;
            }
            default: {
                //do nothing
            }
        }
        auditLog.info("markStarting", changed);
        return changed;
    }

    @OnEventHandler(propagate = false)
    public boolean publishStartTasks(PublishStopTask publishStartTask) {
        if (getStatus() == Service.Status.WAITING_FOR_PARENTS_TO_STOP && (!hasParents() || areAllParentsStopped())) {
            stopService();
        }
        return false;
    }

    @Override
    @NoPropagateFunction
    public void stopOrder(Consumer<ServiceOrderRecord<?>> serviceConsumer) {
        serviceConsumer.accept(new ServiceOrderRecord<>(getServiceName(), getWrappedInstance(), getStatus()));
    }

    @OnEventHandler(filterVariable = "serviceName")
    public boolean notifyServiceStopped(GraphEvent.NotifyServiceStopped statusUpdate) {
        boolean changed = getStatus() != Service.Status.STOPPED;
        if (changed) {
            setStatus(Service.Status.STOPPED);
        }
        return changed;
    }

    @OnTrigger
    public boolean recalculateStatusForStart() {
        if (getStatus() == Service.Status.WAITING_FOR_PARENTS_TO_STOP &&  areAllParentsStopped() ) {
            stopService();
            return true;
        }else if(getStatus() != STARTED && getParentStatusSet().contains(WAITING_FOR_PARENTS_TO_START)){
            setStatus(WAITING_FOR_PARENTS_TO_START);
            return true;
        }
        return true;
    }


}
