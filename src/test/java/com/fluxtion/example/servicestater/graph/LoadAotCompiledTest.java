package com.fluxtion.example.servicestater.graph;

import com.fluxtion.compiler.generation.OutputRegistry;
import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceOrderRecord;
import com.fluxtion.example.servicestater.ServiceStatusRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LoadAotCompiledTest extends BaseServiceStarterTest {
    public static final String AAAAA = "AAAAA";
    private static boolean Astarted = false;
    private static boolean Bstarted = false;
    private static String outputDirectory = OutputRegistry.JAVA_TESTGEN_DIR;

    @BeforeEach
    public void init() {
        Astarted = false;
        Bstarted = false;
    }

    private static ServiceManager buildModel(String className) {
        Service a = Service.builder("A").startTask(LoadAotCompiledTest::startA).wrappedInstance(AAAAA).build();
        Service b = Service.builder("B").requiredServices(a).startTask(LoadAotCompiledTest::startB).build();
        return ServiceManager.compileServiceManagerAot(
                outputDirectory,
                className,
                "com.fluxtion.example.servicestater.testgenerated",
                a, b);
    }

    @Test
    public void testStartCompileAot() {
        Astarted = false;
        Bstarted = false;
        ServiceManager serviceManager = buildModel("ProcessorGetStartOrder");
        validateServiceStarter(serviceManager);
    }

    @Test
    public void testStartInterpreted() {
        Astarted = false;
        Bstarted = false;
        Service a = Service.builder("A").startTask(LoadAotCompiledTest::startA).wrappedInstance(AAAAA).build();
        Service b = Service.builder("B").requiredServices(a).startTask(LoadAotCompiledTest::startB).build();
        ServiceManager serviceManager = ServiceManager.build(a, b);
        validateServiceStarter(serviceManager);

    }

    @Test
    public void testStartCompiledInMemory() {
        Astarted = false;
        Bstarted = false;
        Service a = Service.builder("A").startTask(LoadAotCompiledTest::startA).wrappedInstance(AAAAA).build();
        Service b = Service.builder("B").requiredServices(a).startTask(LoadAotCompiledTest::startB).build();
        ServiceManager serviceManager = ServiceManager.compiledServiceManager(a, b);
        validateServiceStarter(serviceManager);
    }

    @Test
    public void testStartCompiled() {
        Astarted = false;
        Bstarted = false;
        outputDirectory = OutputRegistry.JAVA_TEST_SRC_DIR;
        ServiceManager serviceManager = buildModel("ProcessorTestLoad");
//        ServiceManager serviceManager = ServiceManager.fromProcessor(new ProcessorTestLoad());
        validateServiceStarter(serviceManager);

    }

    private void validateServiceStarter(ServiceManager serviceManager) {
        Assertions.assertFalse(Astarted);
        Assertions.assertFalse(Bstarted);

        serviceManager.registerStatusListener(this::recordServiceStatus);
        HashMap<String, ServiceStatusRecord> statusMap = new HashMap<>();
        updateStatus(statusMap, "A", Service.Status.STATUS_UNKNOWN);
        updateStatus(statusMap, "B", Service.Status.STATUS_UNKNOWN);
        checkStatusMatch(statusMap);

        serviceManager.startService("B");
        updateStatus(statusMap, "A", Service.Status.STARTING);
        updateStatus(statusMap, "B", Service.Status.WAITING_FOR_PARENTS_TO_START);
        Assertions.assertTrue(Astarted);
        Assertions.assertFalse(Bstarted);

        serviceManager.serviceStarted("A");
        updateStatus(statusMap, "A", Service.Status.STARTED);
        updateStatus(statusMap, "B", Service.Status.STARTING);
        Assertions.assertTrue(Astarted);
        Assertions.assertTrue(Bstarted);

        ArrayList<ServiceOrderRecord<?>> list = new ArrayList<>();
        serviceManager.startOrder(list::add);
        Assertions.assertIterableEquals(
                List.of(AAAAA),
                list.stream().map(ServiceOrderRecord::getWrappedInstance).filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList())
        );
    }


    public static void startA() {
        Astarted = true;
    }

    public static void startB() {
        Bstarted = true;
    }
}
