package com.fluxtion.example.servicestater.impl;

import com.fluxtion.example.servicestater.ServiceEvent;
import com.fluxtion.example.servicestater.ServiceStatus;
import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.Initialise;
import com.fluxtion.runtim.annotations.OnEvent;
import com.fluxtion.runtim.annotations.PushReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fluxtion.example.servicestater.FluxtionSystemManager.toStartServiceName;
import static com.fluxtion.example.servicestater.FluxtionSystemManager.toStopServiceName;
import static com.fluxtion.example.servicestater.ServiceStatus.*;

/**
 * Wraps the representation of an external {@link com.fluxtion.example.servicestater.Service}. The {@link ServiceController}
 * is the data structure maintained by the graph i.e. it is a node in the graph. Reacts to events:
 * <ul>
 *     <li>{@link ServiceEvent.StatusUpdate} </li>
 *     <li>{@link ServiceEvent.Start} </li>
 *     <li>{@link ServiceEvent.Stop} </li>
 * </ul>
 *
 * If a Service command can be executed because the dependency requirements are met then the command to execute will
 * be published {@link CommandPublisher}, for a client app to execute after the graph cycle has completed.
 */
public abstract class ServiceController implements Named {

    protected final String serviceName;
    protected final transient String controllerName;
    private List<ServiceController> dependencies = new ArrayList<>();
    @PushReference
    private final CommandPublisher commandPublisher;
    @PushReference
    private final SharedServiceStatus sharedServiceStatus;

    public ServiceController(String serviceName, String controllerName, CommandPublisher commandPublisher, SharedServiceStatus sharedServiceStatus) {
        this.serviceName = serviceName;
        this.controllerName = controllerName;
        this.commandPublisher = commandPublisher;
        this.sharedServiceStatus = sharedServiceStatus;
    }

    public void addDependency(ServiceController dependency) {
        dependencies.add(dependency);
    }

    public final void setDependencies(List<ServiceController> dependencies) {
        this.dependencies = dependencies;
    }

    public final List<ServiceController> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public final ServiceStatus getStatus() {
        return sharedServiceStatus.getStatus(getServiceName());
    }

    public String getServiceName() {
        return serviceName;
    }

    protected void setStatus(ServiceStatus serviceStatus) {
        sharedServiceStatus.setServiceStatus(getServiceName(), serviceStatus);
    }

    protected void publishCommand(ServiceEvent.Command command) {
        commandPublisher.publishCommand(command);
    }

    protected boolean areAllParentsStarted() {
        return getDependencies().stream().map(ServiceController::getStatus).allMatch(ServiceStatus.STARTED::equals);
    }


    protected boolean areAllParentsStopped() {
        return getDependencies().stream().map(ServiceController::getStatus).allMatch(STOPPED::equals);
    }

    protected void startService(){
        publishCommand(new ServiceEvent.Start(getServiceName()));
        setStatus(ServiceStatus.STARTING);
    }


    protected void stopService(){
        publishCommand(new ServiceEvent.Stop(getServiceName()));
        setStatus(ServiceStatus.STOPPING);
    }

    @Override
    public String getName() {
        return controllerName;
    }

    @EventHandler(filterVariable = "serviceName")
    public boolean statusUpdate(ServiceEvent.StatusUpdate statusUpdate) {
        boolean changed = getStatus()!=statusUpdate.getStatus();
        if(changed){
            setStatus(statusUpdate.getStatus());
        }
        return changed;
    }


    @Initialise
    public final void initialise() {
        setStatus(STATUS_UNKNOWN);
    }

    public static class StartServiceController extends ServiceController {
        public StartServiceController(String serviceName, CommandPublisher commandPublisher, SharedServiceStatus sharedServiceStatus) {
            super(serviceName, toStartServiceName(serviceName), commandPublisher, sharedServiceStatus);
        }

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

        @OnEvent
        public boolean recalculateStatusForStart() {
            if (getStatus()==WAITING_FOR_PARENTS_TO_START && areAllParentsStarted()) {
                startService();
                return false;
            }else if(!areAllParentsStarted() && (getStatus()==STARTING || getStatus()==STARTED)){
                stopService();
            }
            return true;
        }

    }

    public static class StopServiceController extends ServiceController {
        public StopServiceController(String serviceName, CommandPublisher commandPublisher, SharedServiceStatus sharedServiceStatus) {
            super(serviceName, toStopServiceName(serviceName), commandPublisher, sharedServiceStatus);
        }

        @EventHandler(propagate = false)
        public boolean stopService(ServiceEvent.Stop startCommand) {
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

        @OnEvent
        public boolean recalculateStatusForStop() {
            if (getStatus()==WAITING_FOR_PARENTS_TO_STOP && areAllParentsStopped()) {
                stopService();
                return false;
            }
            return true;
        }

    }
}
