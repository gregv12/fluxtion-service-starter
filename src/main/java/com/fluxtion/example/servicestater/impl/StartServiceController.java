package com.fluxtion.example.servicestater.impl;

import com.fluxtion.example.servicestater.ServiceEvent;
import com.fluxtion.example.servicestater.ServiceStatus;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.OnEvent;

import static com.fluxtion.example.servicestater.FluxtionSystemManager.toStartServiceName;
import static com.fluxtion.example.servicestater.ServiceStatus.*;

/**
 * A controller for starting services in the graph, based upon the topological order of the nodes in the graph. Reacts to events:
 * <ul>
 *   <li>{@link ServiceEvent.Start} </li>
 *   <li>Change notifications to parent {@link ServiceController} nodes</li>
 * </ul
 */
public class StartServiceController extends ServiceController {
    public StartServiceController(String serviceName, CommandPublisher commandPublisher, SharedServiceStatus sharedServiceStatus) {
        super(serviceName, toStartServiceName(serviceName), commandPublisher, sharedServiceStatus);
    }

    /**
     * Injection point for external start events into this instance. Fluxtion will route events to this instance.
     *
     * @param startCommand Command to start this service
     * @return flag indicating change of state, true - propagate notification to child nodes
     */
    @EventHandler(propagate = false)
    public boolean startService(ServiceEvent.Start startCommand) {
        ServiceStatus startStatus = getStatus();
        switch (startStatus) {
            case STATUS_UNKNOWN:
            case WAITING_FOR_PARENTS_TO_STOP:
            case STOPPING:
            case STOPPED:
                if (getDependencies().isEmpty() || areAllParentsStarted()) {
                    startService();
                } else {
                    setStatus(ServiceStatus.WAITING_FOR_PARENTS_TO_START);
                }
            default:
                //do nothing
        }
        return startStatus != getStatus();
    }

    /**
     * Recalculate when a parent has changed state and notified this node.
     * @return flag indicating change of state, true - propagate notification to child nodes
     */
    @OnEvent
    public boolean recalculateStatusForStart() {
        if (getStatus() == WAITING_FOR_PARENTS_TO_START && areAllParentsStarted()) {
            startService();
            return false;
        } else if (!areAllParentsStarted() && (getStatus() == STARTING || getStatus() == STARTED)) {
            stopService();
        }
        return true;
    }

}
