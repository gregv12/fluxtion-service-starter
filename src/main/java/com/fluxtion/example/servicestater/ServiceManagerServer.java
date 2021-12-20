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
    private final FluxtionServiceManager fluxtionServiceManager;
    private static final LongAdder COUNT = new LongAdder();

    public static ServiceManagerServer compiledServer(Service... serviceList){
        FluxtionServiceManager fluxtionServiceManager = new FluxtionServiceManager();
        fluxtionServiceManager.compiled(true);
        fluxtionServiceManager.buildServiceController(serviceList);
        return new ServiceManagerServer(fluxtionServiceManager);
    }

    public static ServiceManagerServer interpretedServer(Service... serviceList){
        FluxtionServiceManager fluxtionServiceManager = new FluxtionServiceManager();
        fluxtionServiceManager.compiled(false);
        fluxtionServiceManager.buildServiceController(serviceList);
        return new ServiceManagerServer(fluxtionServiceManager);
    }

    private ServiceManagerServer(FluxtionServiceManager fluxtionServiceManager) {
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
        publisher.submit(FluxtionServiceManager::shutdown);
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

    public void registerStatusListener(Consumer<List<ServiceStatusRecord>> publishStatusToLog) {
        publisher.submit(f -> f.registerStatusListener(publishStatusToLog));
    }

    /**
     * Controls the blocking behaviour of this ServiceManagerServer while processing start/stop tasks:
     * <ul>
     *     <li>true - will not process any client requests until all the start/stop tasks are complete</li>
     *     <li>false - start/stop tasks are processed asynchronously, the ServiceManagerServer is free to process
     *     new requests while the tasks are executing</li>
     * </ul>
     * @param waitForTasks flag controlling blocking behaviour while executing tasks
     */
    public void waitForTasksToComplete(boolean waitForTasks){
        publisher.submit(f -> f.waitForTasksToComplete(waitForTasks));
    }
}
