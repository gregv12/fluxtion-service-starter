package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.OnEvent;

import static com.fluxtion.example.servicestater.graph.FluxtionServiceManager.toStartServiceName;
import static com.fluxtion.example.servicestater.Service.Status.STARTED;
import static com.fluxtion.example.servicestater.Service.Status.WAITING_FOR_PARENTS_TO_START;

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
 * </ul
 */
public class ForwardPassServiceController extends ServiceController {
    public ForwardPassServiceController(String serviceName, TaskWrapperPublisher taskWrapperPublisher, ServiceStatusRecordCache serviceStatusRecordCache) {
        super(serviceName, toStartServiceName(serviceName), taskWrapperPublisher, serviceStatusRecordCache);
    }

    @EventHandler
    public boolean startAllServices(GraphEvent.RequestStartAll startAll){
        return startServiceRequest();
    }

    @EventHandler(filterVariable = "serviceName")
    public boolean startThisService(GraphEvent.RequestServiceStart startSingleService) {
        return startServiceRequest();
    }

    private boolean startServiceRequest() {
        boolean changed = false;
        Service.Status startStatus = getStatus();
        switch (startStatus) {
            case STATUS_UNKNOWN, WAITING_FOR_PARENTS_TO_STOP, STOPPING, STOPPED -> {
                setStatus(Service.Status.WAITING_FOR_PARENTS_TO_START);
                changed = true;
            }
            default -> {
                //do nothing
            }
        }
        auditLog.info("markStarting", changed);
        return changed;
    }

    @EventHandler(propagate = false)
    public void publishStartTasks(GraphEvent.PublishStopTask publishStartTask) {
        if (getStatus() == Service.Status.WAITING_FOR_PARENTS_TO_STOP && (!hasParents() || areAllParentsStopped())) {
            stopService();
        }
    }

    @EventHandler(filterVariable = "serviceName")
    public boolean notifyServiceStopped(GraphEvent.NotifyServiceStopped statusUpdate) {
        boolean changed = getStatus() != Service.Status.STOPPED;
        if (changed) {
            setStatus(Service.Status.STOPPED);
        }
        return changed;
    }

    @OnEvent
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
