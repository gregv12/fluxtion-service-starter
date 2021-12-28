package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.helpers.AsynchronousTaskExecutor;
import com.fluxtion.example.servicestater.helpers.SynchronousTaskExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public class ThreadingTests {

    static CountDownLatch countDownLatch;
    static CountDownLatch countDownLatch2;
    static LongAdder adder;
    private static FluxtionServiceManager svcManager;
    static List<Thread> threadNames = new ArrayList<>();

    @BeforeEach
    public void init(){
        countDownLatch = new CountDownLatch(1);
        countDownLatch2 = new CountDownLatch(1);
        adder = new LongAdder();
        threadNames.clear();
    }

    @Test
    public void testSynchronousExecutor() {
        Service svc_1 = Service.builder("svc_1")
                .startTask(ThreadingTests::start1Task)
                .build();
        Service svc_2 = Service.builder("svc_2")
                .serviceListThatRequireMe(List.of(svc_1))
                .startTask(ThreadingTests::start2Task)
                .build();
        svcManager = new FluxtionServiceManager();
        svcManager.compiled(false);
        svcManager.registerTaskExecutor(new SynchronousTaskExecutor());
        svcManager.buildServiceController(svc_1, svc_2);
        svcManager.registerStatusListener(FluxtionServiceManagerModelATest::logStatus);
        svcManager.startService("svc_1");
        assertThat(threadNames.size(), is(2));
        assertThat(Thread.currentThread(), is(threadNames.get(0)));
        assertThat(Thread.currentThread(), is(threadNames.get(1)));
    }

    @SneakyThrows
    @Test
    public void asynchronousExecutor(){
        Service svc_1 = Service.builder("svc_1")
                .startTask(ThreadingTests::asynchSvc1Task)
                .build();
        Service svc_2 = Service.builder("svc_2")
                .serviceListThatRequireMe(List.of(svc_1))
                .startTask(ThreadingTests::blockSvc2Task)
                .build();
        svcManager = new FluxtionServiceManager();
        svcManager.compiled(false);
        svcManager.registerTaskExecutor(new AsynchronousTaskExecutor());
        svcManager.buildServiceController(svc_1, svc_2);
        svcManager.registerStatusListener(FluxtionServiceManagerModelATest::logStatus);
        svcManager.startService("svc_1");
        assertThat(adder.intValue(), is(0));
        log.info("countDownLatch.countDown");
        countDownLatch.countDown();
        log.info("countDownLatch2.countDown");
        countDownLatch2.await();
        assertThat(adder.intValue(), is(1));
    }

    public static void start1Task(){
        threadNames.add(Thread.currentThread());
    }

    public static void start2Task(){
        threadNames.add(Thread.currentThread());
        svcManager.serviceStarted("svc_2");
    }

    @SneakyThrows
    public static void blockSvc2Task(){
        log.info("blocking svc2");
        countDownLatch.await();
        log.info("released svc2");
        svcManager.serviceStarted("svc_2");
    }

    public static void asynchSvc1Task(){
        adder.increment();
        countDownLatch2.countDown();
    }

    public void countDown(){
        countDownLatch.countDown();
    }
}
