package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.graph.FluxtionServiceManager;
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

    private final SubmissionPublisher<Consumer<FluxtionServiceManager>> publisher;
    private final ExecutorService executorService;
    private volatile FluxtionServiceManager manager;
    private static final LongAdder COUNT = new LongAdder();

    public ServiceManagerServer() {
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "serviceManagerThread-" + COUNT.intValue());
            COUNT.increment();
            return thread;
        });
        publisher = new SubmissionPublisher<>(executorService, 1);
        publisher.consume(i -> i.accept(manager));
    }

    public void shutdown(){
        publisher.submit(FluxtionServiceManager::shutdown);
        publisher.close();
        executorService.shutdown();
        log.info("server shutdown");
    }

    public void setManager(FluxtionServiceManager manager) {
        this.manager = manager;
    }

    public void startService(String serviceName) {
        publisher.submit(f -> f.startService(serviceName));
    }

    public void stopService(String serviceName) {
        publisher.submit(f -> f.stopService(serviceName));
    }

    public void startAllServices() {
        publisher.submit(FluxtionServiceManager::startAllServices);
    }

    public void stopAllServices() {
        publisher.submit(FluxtionServiceManager::stopAllServices);
    }

    public void publishServiceStatus() {
        publisher.submit(FluxtionServiceManager::publishAllServiceStatus);
    }

    public void serviceStartedNotification(String serviceName) {
        publisher.submit(f -> f.serviceStartedNotification(serviceName));
    }

    public void serviceStoppedNotification(String serviceName) {
        publisher.submit(f -> f.serviceStoppedNotification(serviceName));
    }

    public void registerStatusListener(Consumer<List<StatusForService>> publishStatusToLog) {
        publisher.submit(f -> f.registerStatusListener(publishStatusToLog));
    }
}
