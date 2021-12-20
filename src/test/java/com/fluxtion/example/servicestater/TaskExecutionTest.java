package com.fluxtion.example.servicestater;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class TaskExecutionTest {
    static ServiceManagerServer server;

    static CountDownLatch countDownLatch;
    static ExecutorService executorService;

    @Test
    public void testSynchronousTaskExecution() throws InterruptedException {
        FluxtionServiceManagerModelATest.auditOn(false);
        countDownLatch = new CountDownLatch(1);
        executorService = Executors.newCachedThreadPool();
        //two tasks that are auto triggered to run in parallel
        //The first task will trigger a dependent service to start
        //The second parallel task is slow the system will not process the dependent start until both parallel tasks have completed
        Service finishService = new Service("finishService", TaskExecutionTest::releaseTest, null);
        Service parallel_2 = new Service("parallel_2", TaskExecutionTest::parallel_2_sleep_3_seconds, null, finishService);
        Service parallel_1 = new Service("parallel_1", TaskExecutionTest::parallel_1_immediate, null, finishService);
        Service rootService = new Service("rootTask", TaskExecutionTest::triggerBothParallels, null, parallel_1, parallel_2);
        server = ServiceManagerServer.interpretedServer(rootService, parallel_1, parallel_2, finishService);
//        server.registerStatusListener(FluxtionServiceManagerModelATest::logStatus);


        //kick off the tasks - will cause all the sub tasks to be running before starting
        server.startService("finishService");

        //stop test exiting early
        countDownLatch.await();
    }

    public static void triggerBothParallels(){
        log.info("ROOT::completed");
        executorService.submit(() ->server.serviceStartedNotification("rootTask"));
    }

    @SneakyThrows
    public static void parallel_2_sleep_3_seconds(){
        log.info("PARALLEL_2::sleeping");
        Thread.sleep(3_000);
        log.info("PARALLEL_2::completed");
        executorService.submit(() ->server.serviceStartedNotification("parallel_2"));
    }

    public static void parallel_1_immediate(){
        log.info("PARALLEL_1:: completed");
        executorService.submit(() ->server.serviceStartedNotification("parallel_1"));
    }

    public static void releaseTest(){
        log.info("FINISHSERVICE::executing delayed task!!!");
        executorService.submit(() ->server.serviceStartedNotification("finishService"));
        countDownLatch.countDown();
    }
}
