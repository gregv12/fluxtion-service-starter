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
import com.fluxtion.example.servicestater.TaskWrapper;
import com.fluxtion.example.servicestater.graph.GraphEvent.RemoveService;
import com.fluxtion.runtime.annotations.Initialise;
import com.fluxtion.runtime.annotations.OnEventHandler;
import com.fluxtion.runtime.annotations.PushReference;
import com.fluxtion.runtime.audit.EventLogNode;
import com.fluxtion.runtime.node.NamedNode;
import com.fluxtion.runtime.partition.LambdaReflection;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
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
@ToString
public abstract class ServiceController extends EventLogNode implements NamedNode {

    protected final String serviceName;
    protected final transient String controllerName;
    @PushReference
    private final TaskWrapperPublisher taskWrapperPublisher;
    @PushReference
    private final ServiceStatusRecordCache serviceStatusRecordCache;
    /**
     * services that depend up on this instance
     */
    private List<ServiceController> dependents = new ArrayList<>();
    private LambdaReflection.SerializableRunnable startTask;
    private LambdaReflection.SerializableRunnable stopTask;

    public ServiceController(String serviceName, String controllerName, TaskWrapperPublisher taskWrapperPublisher, ServiceStatusRecordCache serviceStatusRecordCache) {
        this.serviceName = serviceName;
        this.controllerName = controllerName;
        this.taskWrapperPublisher = taskWrapperPublisher;
        this.serviceStatusRecordCache = serviceStatusRecordCache;
    }

    void addDependent(ServiceController dependency) {
        if (!dependents.contains(dependency)) {
            dependents.add(dependency);
        }

    }

    @OnEventHandler(propagate = false)
    public boolean removeDependent(RemoveService removeServiceEvent){
        dependents.removeIf(removeServiceEvent::serviceMatch);
        return false;
    }

    public final List<ServiceController> getDependents() {
        return Collections.unmodifiableList(dependents);
    }

    public final void setDependents(List<ServiceController> dependents) {
        this.dependents = dependents;
    }

    public final Service.Status getStatus() {
        return serviceStatusRecordCache.getStatus(getServiceName());
    }

    protected void setStatus(Service.Status status) {
        auditLog.info("initialStatus", getStatus());
        auditLog.info("setStatus", status);
        serviceStatusRecordCache.setServiceStatus(getServiceName(), status);
    }

    public String getServiceName() {
        return serviceName;
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
        return getDependents().stream().map(ServiceController::getStatus).allMatch(Service.Status.STARTED::equals);
    }

    protected boolean areAllParentsStopped() {
        return getDependents().stream().map(ServiceController::getStatus).allMatch(Service.Status.STOPPED::equals);
    }

    protected boolean hasParents() {
        return getDependents().size() > 0;
    }

    protected EnumSet<Service.Status> getParentStatusSet() {
        return getDependents().stream()
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
        serviceStatusRecordCache.setServiceStatus(getServiceName(), STATUS_UNKNOWN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceController that = (ServiceController) o;
        return serviceName.equals(that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName);
    }
}
