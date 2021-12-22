package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.TaskWrapper;
import com.fluxtion.runtim.Named;
import com.fluxtion.runtim.annotations.Initialise;
import com.fluxtion.runtim.annotations.PushReference;
import com.fluxtion.runtim.audit.EventLogNode;
import com.fluxtion.runtim.partition.LambdaReflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.fluxtion.example.servicestater.Service.Status.STATUS_UNKNOWN;

/**
 * A base class that wraps the representation of an external {@link com.fluxtion.example.servicestater.Service}. The {@link ServiceController}
 * is the data structure maintained by the graph i.e. it is a node in the graph. Reacts to events:
 * <ul>
 *     <li> </li>
 * </ul>
 * <p>
 * If a Service command can be executed because the dependency requirements are met then the command to execute will
 * be published {@link TaskWrapperPublisher}, for a client app to execute after the graph cycle has completed.
 */
public abstract class ServiceController extends EventLogNode implements Named {

    protected final String serviceName;
    protected final transient String controllerName;
    private List<ServiceController> dependencies = new ArrayList<>();
    @PushReference
    private final TaskWrapperPublisher taskWrapperPublisher;
    @PushReference
    private final ServiceStatusRecordCache serviceStatusRecordCache;
    private LambdaReflection.SerializableRunnable startTask;
    private LambdaReflection.SerializableRunnable stopTask;

    public ServiceController(String serviceName, String controllerName, TaskWrapperPublisher taskWrapperPublisher, ServiceStatusRecordCache serviceStatusRecordCache) {
        this.serviceName = serviceName;
        this.controllerName = controllerName;
        this.taskWrapperPublisher = taskWrapperPublisher;
        this.serviceStatusRecordCache = serviceStatusRecordCache;
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

    public final Service.Status getStatus() {
        return serviceStatusRecordCache.getStatus(getServiceName());
    }

    public String getServiceName() {
        return serviceName;
    }

    protected void setStatus(Service.Status status) {
        auditLog.info("initialStatus", getStatus());
        auditLog.info("setStatus", status);
        serviceStatusRecordCache.setServiceStatus(getServiceName(), status);
    }

    public LambdaReflection.SerializableRunnable getStartTask() {
        return startTask;
    }

    public void setStartTask(LambdaReflection.SerializableRunnable startTask) {
        this.startTask = startTask;
    }

    public LambdaReflection.SerializableRunnable getStopTask() {
        return stopTask;
    }

    public void setStopTask(LambdaReflection.SerializableRunnable stopTask) {
        this.stopTask = stopTask;
    }

    protected void publishTask(TaskWrapper task) {
        taskWrapperPublisher.publishCommand(task);
    }

    protected boolean areAllParentsStarted() {
        return getDependencies().stream().map(ServiceController::getStatus).allMatch(Service.Status.STARTED::equals);
    }

    protected boolean areAllParentsStopped() {
        return getDependencies().stream().map(ServiceController::getStatus).allMatch(Service.Status.STOPPED::equals);
    }

    protected boolean hasParents() {
        return getDependencies().size() > 0;
    }

    protected EnumSet<Service.Status> getParentStatusSet() {
        return getDependencies().stream()
                .map(ServiceController::getStatus)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Service.Status.class)));
    }

    protected void startService() {
        if (getStartTask() != null) {
            publishTask(new TaskWrapper(getServiceName(), true, getStartTask()));
        }
        setStatus(Service.Status.STARTING);
    }

    protected void stopService() {
        if (getStopTask() != null) {
            publishTask(new TaskWrapper(getServiceName(), false, getStopTask()));
        }
        setStatus(Service.Status.STOPPING);
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
