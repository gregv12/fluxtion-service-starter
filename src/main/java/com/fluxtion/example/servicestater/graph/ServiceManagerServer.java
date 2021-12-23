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
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

/**
 * Wraps a ServiceManager to ensure single threaded access to the underlying {@link FluxtionServiceManager}. All method calls
 * are converted to tasks and placed on a task queue for later execution by the {@link FluxtionServiceManager} thread.
 */
@Slf4j
public class ServiceManagerServer {

    private final SubmissionPublisher<Consumer<ServiceManager>> publisher;
    private final ExecutorService executorService;
    private final ServiceManager fluxtionServiceManager;
    private static final LongAdder COUNT = new LongAdder();

    public ServiceManagerServer(ServiceManager fluxtionServiceManager) {
        this.fluxtionServiceManager = fluxtionServiceManager;
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "serviceManagerThread-" + COUNT.intValue());
            COUNT.increment();
            return thread;
        });
        publisher = new SubmissionPublisher<>(executorService, 1);
        publisher.consume(i -> i.accept(this.fluxtionServiceManager));
    }

    public void shutdown(){
        publisher.submit(ServiceManager::shutdown);
        publisher.close();
        executorService.shutdown();
        log.info("server shutdown");
    }

    public void startService(String serviceName) {
        publisher.submit(f -> f.startService(serviceName));
    }

    public void stopService(String serviceName) {
        publisher.submit(f -> f.stopService(serviceName));
    }

    public void startAllServices() {
        publisher.submit(ServiceManager::startAllServices);
    }

    public void stopAllServices() {
        publisher.submit(ServiceManager::stopAllServices);
    }

    public void publishServiceStatus() {
        publisher.submit(ServiceManager::publishSystemStatus);
    }

    public void serviceStartedNotification(String serviceName) {
        publisher.submit(f -> f.serviceStarted(serviceName));
    }

    public void serviceStoppedNotification(String serviceName) {
        publisher.submit(f -> f.serviceStopped(serviceName));
    }

    public void registerStatusListener(Consumer<List<ServiceStatusRecord>> publishStatusToLog) {
        publisher.submit(f -> f.registerStatusListener(publishStatusToLog));
    }

}
