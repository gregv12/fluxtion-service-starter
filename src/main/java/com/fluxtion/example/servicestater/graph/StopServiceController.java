package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.ServiceEvent;
import com.fluxtion.example.servicestater.ServiceStatus;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.OnEvent;

import static com.fluxtion.example.servicestater.ServiceManager.toStopServiceName;
import static com.fluxtion.example.servicestater.ServiceStatus.WAITING_FOR_PARENTS_TO_STOP;

/**
 * A controller for stopping services in the graph, based upon the reverse topological order of the nodes in the graph. Reacts to events:
 * <ul>
 *   <li>{@link ServiceEvent.StopSingleService} </li>
 *   <li>{@link ServiceEvent.StopAllServices} </li>
 *   <li>Change notifications to parent {@link ServiceController} nodes</li>
 * </ul
 */
public class StopServiceController extends ServiceController {
    public StopServiceController(String serviceName, TaskWrapperPublisher taskWrapperPublisher, SharedServiceStatus sharedServiceStatus) {
        super(serviceName, toStopServiceName(serviceName), taskWrapperPublisher, sharedServiceStatus);
    }

    @EventHandler(filterVariable = "serviceName")
    public boolean stopThisService(ServiceEvent.StopSingleService startSingleService){
        return handleStop();
    }

    /**
     * Injection point for external stop events into this instance. Fluxtion will route events to this instance.
     *
     * @param stopSingleServiceCommand Command to stop this service
     * @return flag indicating change of state, true - propagate notification to child nodes
     */
    @EventHandler(propagate = false)
    public boolean stopAllServices(ServiceEvent.StopAllServices stopSingleServiceCommand) {
        return handleStop();
    }

    private boolean handleStop() {
        ServiceStatus startStatus = getStatus();
        switch (startStatus) {
            case STATUS_UNKNOWN:
            case WAITING_FOR_PARENTS_TO_START:
            case STARTING:
            case STARTED:
                if (getDependencies().isEmpty() || areAllParentsStopped()) {
                    stopService();
                } else {
                    setStatus(ServiceStatus.WAITING_FOR_PARENTS_TO_STOP);
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
    public boolean recalculateStatusForStop() {
        if (getStatus() == WAITING_FOR_PARENTS_TO_STOP && areAllParentsStopped()) {
            stopService();
            return false;
        }
        return true;
    }

}
