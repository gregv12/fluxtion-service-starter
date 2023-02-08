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

package com.fluxtion.example.servicestater;

import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Encapsulates a task that a service has provided during registration.
 */
@Data
@Slf4j
public class TaskWrapper implements Callable<TaskWrapper.TaskExecutionResult> {

    private final String serviceName;
    private final boolean startTask;
    private final Runnable task;

    @Override
    public String toString() {
        return "TaskWrapper{" +
                "serviceName='" + serviceName + '\'' +
                ", startTask='" + startTask + '\'' +
                '}';
    }

    @Override
    public TaskExecutionResult call() {
        TaskExecutionResult result;
        try {
            log.info("executing {}", this);
            task.run();
            result = new TaskExecutionResult(true, isStartTask(), getServiceName(), false, null);
        } catch (Exception e) {
            log.warn("problem executing task", e);
            result = new TaskExecutionResult(false, isStartTask(), getServiceName(), true, e);
        }
        log.debug("completed executing: {}", result);
        return result;
    }

    /**
     * Processes {@link TaskWrapper} published by the {@link ServiceManager}
     */
    public interface TaskExecutor extends Consumer<List<TaskWrapper>>, AutoCloseable {

        default void failFast(boolean failFastFlag) {
        }
    }

    @Value
    public static class TaskExecutionResult {
        boolean success;
        boolean startTask;
        String serviceName;
        boolean exceptionThrown;
        Throwable exception;
    }
}
