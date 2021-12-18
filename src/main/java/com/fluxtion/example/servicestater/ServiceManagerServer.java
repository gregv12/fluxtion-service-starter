package com.fluxtion.example.servicestater;

import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

/**
 * Wraps a ServiceManager to ensure single threaded access to the underlying {@link ServiceManager}. All method calls
 * are converted to task and placed on a task queue for later execution by the {@link ServiceManager} thread.
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

    public void traceMethodCalls(boolean traceOn) {
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

    public void publishAllServiceStatus() {
        publisher.submit(ServiceManager::publishAllServiceStatus);
    }

    public void processServiceStartedNotification(String serviceName) {
        publisher.submit(f -> f.processServiceStartedNotification(serviceName));
    }

    public void processServiceStoppedNotification(String serviceName) {
        publisher.submit(f -> f.processServiceStoppedNotification(serviceName));
    }

    public void processStatusUpdate(ServiceEvent.StatusUpdate statusUpdate) {
        publisher.submit(f -> f.processStatusUpdate(statusUpdate));
    }
}
