package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.ServiceEvent;
import com.fluxtion.example.servicestater.ServiceStatus;
import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.annotations.EventHandler;
import com.fluxtion.runtim.annotations.Initialise;
import com.fluxtion.runtim.annotations.PushReference;
import com.fluxtion.runtim.event.Event;
import com.fluxtion.runtim.partition.LambdaReflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fluxtion.example.servicestater.ServiceStatus.STATUS_UNKNOWN;
import static com.fluxtion.example.servicestater.ServiceStatus.STOPPED;

/**
 * A base class that wraps the representation of an external {@link com.fluxtion.example.servicestater.Service}. The {@link ServiceController}
 * is the data structure maintained by the graph i.e. it is a node in the graph. Reacts to events:
 * <ul>
 *     <li>{@link ServiceEvent.StatusUpdate} </li>
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
    private LambdaReflection.SerializableRunnable task;

    public ServiceController(String serviceName, String controllerName, CommandPublisher commandPublisher, SharedServiceStatus sharedServiceStatus) {
        this.serviceName = serviceName;
        this.controllerName = controllerName;
        this.commandPublisher = commandPublisher;
        this.sharedServiceStatus = sharedServiceStatus;
    }

    /**
     * Injection point for external statusUpdate events into this instance. Fluxtion will route events to this instance
     * where the {@link Event#filterString()} matches the serviceName variable of this instance.
     *
     * @param statusUpdate Command to the state of this service
     * @return flag indicating change of state, true - propagate notification to child nodes
     */
    @EventHandler(filterVariable = "serviceName")
    public boolean statusUpdate(ServiceEvent.StatusUpdate statusUpdate) {
        boolean changed = getStatus()!=statusUpdate.getStatus();
        if(changed){
            setStatus(statusUpdate.getStatus());
        }
        return changed;
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

    public LambdaReflection.SerializableRunnable getTask() {
        return task;
    }

    public void setTask(LambdaReflection.SerializableRunnable task) {
        this.task = task;
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
        publishCommand(new ServiceEvent.StartSingleService(getServiceName()));
        setStatus(ServiceStatus.STARTING);
    }

    protected void stopService(){
        publishCommand(new ServiceEvent.StopSingleService(getServiceName()));
        setStatus(ServiceStatus.STOPPING);
    }

    @Override
    public String getName() {
        return controllerName;
    }


    @Initialise
    public final void initialise() {
        setStatus(STATUS_UNKNOWN);
    }

}
