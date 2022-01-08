package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class TaskExecutionTest {
    static ServiceManager server;
    static CountDownLatch countDownLatch;
    static ExecutorService executorService;

    //TODO add assert for this test
    @Test
//    @Disabled("does not actually test only demonstrates, needs to be updated")
    public void testSynchronousTaskExecution() throws InterruptedException {
        BaseServiceStarterTest.auditOn(false);
        countDownLatch = new CountDownLatch(1);
        executorService = Executors.newCachedThreadPool();
        //two tasks that are auto triggered to run in parallel
        //The first task will trigger a dependent service to start
        //The second parallel task is slow the system will not process the dependent start until both parallel tasks have completed
        Service finishService = Service.builder("finishService")
                .startTask(TaskExecutionTest::releaseTest)
                .build();
        Service parallel_2 = Service.builder("parallel_2")
                .startTask(TaskExecutionTest::parallel_2_sleep_3_seconds)
                .serviceListThatRequireMe(List.of(finishService))
                .build();
        Service parallel_1 = Service.builder("parallel_1")
                .startTask(TaskExecutionTest::parallel_1_immediate)
                .serviceListThatRequireMe(List.of(finishService))
                .build();
        Service rootService = Service.builder("rootService")
                .startTask(TaskExecutionTest::triggerBothParallels)
                .serviceListThatRequireMe(List.of(parallel_1, parallel_2))
                .build();
        server = ServiceManager.build(rootService, parallel_1, parallel_2, finishService);
//        server.registerStatusListener(FluxtionServiceManagerModelATest::logStatus);
        //kick off the tasks - will cause all the sub tasks to be running before starting
        server.startService("finishService");

        //stop test exiting early
        countDownLatch.await();
    }

    public static void triggerBothParallels(){
        log.info("ROOT::completed");
        executorService.submit(() ->server.serviceStarted("rootService"));
    }

    @SneakyThrows
    public static void parallel_2_sleep_3_seconds(){
        log.info("PARALLEL_2::sleeping");
        Thread.sleep(100);
        log.info("PARALLEL_2::completed");
        executorService.submit(() ->server.serviceStarted("parallel_2"));
    }

    public static void parallel_1_immediate(){
        log.info("PARALLEL_1:: completed");
        executorService.submit(() ->server.serviceStarted("parallel_1"));
    }

    public static void releaseTest(){
        log.info("FINISHSERVICE::executing delayed task!!!");
        executorService.submit(() ->server.serviceStarted("finishService"));
        countDownLatch.countDown();
    }
}
