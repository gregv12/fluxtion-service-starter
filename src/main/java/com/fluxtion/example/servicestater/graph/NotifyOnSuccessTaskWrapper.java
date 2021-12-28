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

import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.TaskWrapper;

/**
 * Wraps a {@link TaskWrapper} and if the task executes without exception notifies the {@link ServiceManager} that the service
 * has stopped or started with either:
 * <ul>
 *     <li>{@link ServiceManager#serviceStarted(String)}</li>
 *     <li>{@link ServiceManager#serviceStopped(String)}</li>
 * </ul>
 *
 * {@link TaskWrapper#isStartTask()} determines which notification to send to the {@link ServiceManager}
 */
class NotifyOnSuccessTaskWrapper extends TaskWrapper {
    private final ServiceManager serviceManager;

    public NotifyOnSuccessTaskWrapper(TaskWrapper taskWrapper, ServiceManager serviceManager) {
        super(taskWrapper.getServiceName(), taskWrapper.isStartTask(), taskWrapper.getTask());
        this.serviceManager = serviceManager;
    }

    @Override
    public TaskExecutionResult call() {
        TaskExecutionResult result = super.call();
        if (result.isSuccess() && isStartTask()) {
            serviceManager.serviceStarted(getServiceName());
        } else if (result.isSuccess() && !isStartTask()) {
            serviceManager.serviceStopped(getServiceName());
        }
        return result;
    }
}
