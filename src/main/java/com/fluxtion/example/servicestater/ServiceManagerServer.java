package com.fluxtion.example.servicestater;

import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

/**
 * Wraps a ServiceManager to ensure single threaded access to the underlying {@link ServiceManager}. All method calls
 * are converted to tasks and placed on a task queue for later execution by the {@link ServiceManager} thread.
 */
public class ServiceManagerServer {

    private final SubmissionPublisher<Consumer<ServiceManager>> publisher;
    private volatile ServiceManager manager;

    public ServiceManagerServer() {
        publisher = new SubmissionPublisher<>();
        publisher.consume(i -> i.accept(manager));
    }

    public void setManager(ServiceManager manager) {
        this.manager = manager;
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
        publisher.submit(ServiceManager::publishAllServiceStatus);
    }

    public void serviceStartedNotification(String serviceName) {
        publisher.submit(f -> f.serviceStartedNotification(serviceName));
    }

    public void serviceStoppedNotification(String serviceName) {
        publisher.submit(f -> f.serviceStoppedNotification(serviceName));
    }

}
