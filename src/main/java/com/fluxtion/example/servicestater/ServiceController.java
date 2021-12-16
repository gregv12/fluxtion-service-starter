package com.fluxtion.example.servicestater;

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

public abstract class ServiceController implements Named {

    protected final String serviceName;
    protected final transient String controllerName;
    private List<ServiceController> dependencies = new ArrayList<>();
    @PushReference
    private final CommandPublisher commandPublisher;
    @PushReference
    private final SharedStatus sharedStatus;

    public ServiceController(String serviceName, String controllerName, CommandPublisher commandPublisher, SharedStatus sharedStatus) {
        this.serviceName = serviceName;
        this.controllerName = controllerName;
        this.commandPublisher = commandPublisher;
        this.sharedStatus = sharedStatus;
    }

    void addDependency(ServiceController dependency) {
        dependencies.add(dependency);
    }

    public final void setDependencies(List<ServiceController> dependencies) {
        this.dependencies = dependencies;
    }

    public final List<ServiceController> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public final ServiceStatus getStatus() {
        return sharedStatus.getStatus(getServiceName());
    }

    public String getServiceName() {
        return serviceName;
    }

    protected void setStatus(ServiceStatus serviceStatus) {
        sharedStatus.setServiceStatus(getServiceName(), serviceStatus);
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
        public StartServiceController(String serviceName, CommandPublisher commandPublisher, SharedStatus sharedStatus) {
            super(serviceName, toStartServiceName(serviceName), commandPublisher, sharedStatus);
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
        public StopServiceController(String serviceName, CommandPublisher commandPublisher, SharedStatus sharedStatus) {
            super(serviceName, toStopServiceName(serviceName), commandPublisher, sharedStatus);
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
