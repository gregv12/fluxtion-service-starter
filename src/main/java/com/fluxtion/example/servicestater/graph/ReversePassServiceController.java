package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.OnEvent;

import static com.fluxtion.example.servicestater.Service.Status.*;
import static com.fluxtion.example.servicestater.graph.FluxtionServiceManager.toStopServiceName;

/**
 * A controller for notifying services in the graph, based upon the reverse topological order of the nodes in the graph.
 * The topological order is with respect to event processing, the top node(s) of the tree with no parents is an
 * event handler. Events enter the graph at the event handler, and notifications pass down the graph.
 * <p>
 * The reverse controller reverses the topological order with the event handlers becoming the last node(s).
 * <p>
 * <p>
 * Reacts to events:
 * <ul>
 *   <li> </li>
 *   <li> </li>
 *   <li>Change notifications to parent {@link ServiceController} nodes</li>
 * </ul
 */
public class ReversePassServiceController extends ServiceController {
    public ReversePassServiceController(String serviceName, TaskWrapperPublisher taskWrapperPublisher, ServiceStatusRecordCache serviceStatusRecordCache) {
        super(serviceName, toStopServiceName(serviceName), taskWrapperPublisher, serviceStatusRecordCache);
    }

    private boolean justStarted = false;

    @EventHandler(filterVariable = "serviceName")
    public boolean stopThisService(GraphEvent.RequestServiceStop serviceStopRequest){
        boolean changed = false;
        Service.Status initialStatus = getStatus();
        switch (initialStatus) {
            case STATUS_UNKNOWN, WAITING_FOR_PARENTS_TO_START, STARTING, STARTED -> {
                setStatus(Service.Status.WAITING_FOR_PARENTS_TO_STOP);
                changed = true;
            }
            default -> {
                //do nothing
            }
        }
        auditLog.info("markStopping", changed);
        return changed;
    }

    @EventHandler(propagate = false)
    public void publishStartTasks(GraphEvent.PublishStartTask publishStartTask) {
        if (getStatus() == Service.Status.WAITING_FOR_PARENTS_TO_START && (!hasParents() || areAllParentsStarted())) {
            startService();
        }
    }

    @EventHandler(filterVariable = "serviceName")
    public boolean notifyServiceStarted(GraphEvent.NotifyServiceStarted statusUpdate) {
        boolean changed = getStatus() != Service.Status.STARTED;
        if (changed) {
            setStatus(Service.Status.STARTED);
            justStarted = true;
        }
        return changed;
    }


    @OnEvent
    public boolean recalculateStatusForStop() {
        if (getStatus() == Service.Status.WAITING_FOR_PARENTS_TO_START && areAllParentsStarted() ) {
            startService();
            return true;
        }else if(getStatus() != STOPPED && getParentStatusSet().contains(WAITING_FOR_PARENTS_TO_STOP)){
            setStatus(WAITING_FOR_PARENTS_TO_STOP);
            return true;
        } else if(justStarted){
            justStarted = false;
            return true;
        }
        return false;
    }


}
