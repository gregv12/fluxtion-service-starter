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
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
public class ASynchronousTaskExecutor implements TaskWrapper.TaskExecutor {

    private final ExecutorService executorService;
    private static final LongAdder COUNT = new LongAdder();

    public ASynchronousTaskExecutor() {
        executorService = Executors.newCachedThreadPool(this::namedThreadFactory);
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    @Override
    public void accept(List<TaskWrapper> taskWrapper) {
        taskWrapper.forEach(executorService::submit);
    }

    private Thread namedThreadFactory(Runnable runnable) {
        Thread thread = new Thread(runnable, "taskExecutor-" + COUNT.intValue());
        COUNT.increment();
        return thread;
    }
}
