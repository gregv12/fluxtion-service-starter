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

package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.example.servicestater.TaskWrapper;
import com.fluxtion.example.servicestater.TaskWrapper.TaskExecutionResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SynchronousTaskExecutor implements TaskWrapper.TaskExecutor {

    private boolean failOnException;

    public SynchronousTaskExecutor(boolean failOnException) {
        this.failOnException = failOnException;
    }

    public void failFast(boolean failFastFlag) {
        this.failOnException = failFastFlag;
    }

    @Override
    public void close() {
        //do nothing
    }

    @SneakyThrows
    @Override
    public void accept(List<TaskWrapper> taskWrapper) {
        for (TaskWrapper wrapper : taskWrapper) {
            TaskExecutionResult taskExecutionResult = wrapper.call();
            if(failOnException && taskExecutionResult.isExceptionThrown()){
                throw taskExecutionResult.getException();
            }
        }
    }

}
