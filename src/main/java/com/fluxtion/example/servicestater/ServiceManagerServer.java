package com.fluxtion.example.servicestater;

import com.fluxtion.example.servicestater.graph.FluxtionServiceManager;

import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

/**
 * Wraps a ServiceManager to ensure single threaded access to the underlying {@link FluxtionServiceManager}. All method calls
 * are converted to tasks and placed on a task queue for later execution by the {@link FluxtionServiceManager} thread.
 */
public class ServiceManagerServer {

    private final SubmissionPublisher<Consumer<FluxtionServiceManager>> publisher;
    private volatile FluxtionServiceManager manager;

    public ServiceManagerServer() {
        publisher = new SubmissionPublisher<>();
        publisher.consume(i -> i.accept(manager));
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

}
