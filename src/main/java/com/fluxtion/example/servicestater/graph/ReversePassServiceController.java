package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.OnEvent;

import static com.fluxtion.example.servicestater.ServiceManager.toStopServiceName;

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
    public ReversePassServiceController(String serviceName, TaskWrapperPublisher taskWrapperPublisher, ServiceStatusCache serviceStatusCache) {
        super(serviceName, toStopServiceName(serviceName), taskWrapperPublisher, serviceStatusCache);
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
        }
        return changed;
    }


    @OnEvent
    public boolean recalculateStatusForStop() {
        if (getStatus() == Service.Status.WAITING_FOR_PARENTS_TO_START &&  areAllParentsStarted() ) {
            startService();
        }
        return true;
    }


}
