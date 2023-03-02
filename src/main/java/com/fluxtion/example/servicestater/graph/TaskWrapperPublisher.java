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
import com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterCommandProcessor;
import com.fluxtion.runtime.annotations.AfterEvent;
import com.fluxtion.runtime.annotations.OnEventHandler;
import com.fluxtion.runtime.node.NamedNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Publishes commands for a service that are for execution by client code, a client application registers a command executor
 * by calling {@link ServiceManager#registerTaskExecutor(TaskWrapper.TaskExecutor)}
 * <p>
 * The task list of events can be executed in parallel.
 */
public class TaskWrapperPublisher implements NamedNode {

    private transient final List<TaskWrapper> commandList = new ArrayList<>();
    private Consumer<List<TaskWrapper>> commandPublisher = (command -> {
    });

    @OnEventHandler(propagate = false)
    public boolean registerCommandProcessor(RegisterCommandProcessor registerCommandProcessor) {
        this.commandPublisher = registerCommandProcessor.getConsumer();
        return false;
    }

    public void publishCommand(TaskWrapper command) {
        commandList.add(command);
    }

    @AfterEvent
    public void publishCommands() {
        if (!commandList.isEmpty()) {
            commandPublisher.accept(new ArrayList<>(commandList));
            commandList.clear();
        }
    }

    @Override
    public String getName() {
        return "commandPublisher";
    }
}
