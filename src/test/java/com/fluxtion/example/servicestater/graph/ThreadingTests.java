package com.fluxtion.example.servicestater.graph;

import com.fluxtion.example.servicestater.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;

public class ThreadingTests {

    CountDownLatch countDownLatch;
    LongAdder adder;

    @BeforeEach
    public void init(){
        countDownLatch = new CountDownLatch(1);
        adder = new LongAdder();
    }

    @Test
    public void testSynchronisation() {
        Service svc_1 = Service.builder("svc_1").startTask(() -> System.out.println("hello world")).build();
        FluxtionServiceManager svcManager = new FluxtionServiceManager();
        svcManager.compiled(false);
        svcManager.buildServiceController(svc_1);
        svcManager.startService("svc_1");

    }

    public void countDown(){
        countDownLatch.countDown();
    }
}
