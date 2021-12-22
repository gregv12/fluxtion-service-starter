package com.fluxtion.example.servicestater.graph;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.helpers.PublishServiceStatusRecordToLog;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * A command line client that tests a sample service graph loaded into the {@link FluxtionServiceManager}
 * <p>
 * Various cli commands are provided to exercise all the operations on the service manager. Run the program and a help
 * message is displayed detailing the usage.
 */
@Slf4j
public class CliTestClient {

    private static ServiceManager serviceManagerServer;

    @SneakyThrows
    public static void main(String[] args) {
        System.out.println("Welcome to FluxtionService interactive tester - building test service graph");
        System.out.println("===============================================================================");
        auditOn(false);
        buildGraph(false);
        serviceManagerServer.startService("aggAB");
        Scanner scanner = new Scanner(System.in);
        boolean run = true;
        Thread.sleep(100);//give the threads time to start and publish help message at end
        printHelp();
        while (run) {
            System.out.print(">");
            String command = scanner.next().toLowerCase(Locale.ROOT);
            switch (command) {
                case "build":
                case "b": {
                    buildGraph(false);
                }
                case "compile":
                case "c":
                    buildGraph(true);
                case "ss":
                case "status":
                    printStatus();
                case "sa":
                case "startall":
                    startAll();
                case "ha":
                case "stopall":
                    stopAll();
                case "s":
                case "start":
                    startByName(scanner);
                case "h":
                case "stop":
                    stopByName(scanner);
                case "ns":
                    notifiedStartedByName(scanner);
                case "nh":
                    notifiedStoppedByName(scanner);
                case "aon":
                case "auditon":
                    auditOn(true);
                case "auditoff":
                case "aoff":
                    auditOn(false);
                case "printtree":
                    printTree();
                case "pt":
                    printTree();
                case "e":
                case "exit":
                    run = false;
                case "?":
                case "help":
                    printHelp();
                default:
                    System.out.println("unknown command:" + command + " ? for command list");
            }
            scanner.nextLine();
        }
        scanner.close();
        serviceManagerServer.shutdown();
    }

    static void printHelp() {
        String help = "\n" +
                "FluxtionService interactive tester commands:\n" +
                "===============================================\n" +
                "help or ?                 - print this message\n" +
                "build or b                - drops the graph and builds a new interpreted graph from scratch\n" +
                "compile or c              - drops the graph and builds a new graph from scratch, generated and compiles java source code\n" +
                "status or ss              - prints the current status of the graph to console\n" +
                "startAll or sa            - start all services\n" +
                "stopAll or ha             - stop all services\n" +
                "start or s [service name] - start a single services by name\n" +
                "stop or h [service name]  - stop a single service by name\n" +
                "ns [service name]         - notify of started status for a single service by name\n" +
                "nh [service name]         - notify of stopped status for a single service by name\n" +
                "auditOn or aon            - turn audit recording on\n" +
                "auditOff or aoff          - turn audit recording off\n" +
                "printTree or pt           - print the DAG of the test model\n" +
                "exit or e                 - exit the application";
        System.out.println(help);
    }

    private static void printTree() {
        System.out.println(asciiArtDAG);
    }

    public static void startAll() {
        checkControllerIsBuilt();
        serviceManagerServer.startAllServices();
    }

    public static void stopAll() {
        checkControllerIsBuilt();
        serviceManagerServer.stopAllServices();
    }

    private static void printStatus() {
        checkControllerIsBuilt();
        serviceManagerServer.publishSystemStatus();
    }

    private static void checkControllerIsBuilt() {
        if (serviceManagerServer == null) {
            System.out.println("no service manager built, building one first");
            buildGraph(false);
        }
    }

    private static void startByName(Scanner scanner) {
        checkControllerIsBuilt();
        if (scanner.hasNext()) {
            serviceManagerServer.startService(scanner.next());
        } else {
            System.out.println("2nd argument required - service name");
        }
    }

    private static void stopByName(Scanner scanner) {
        checkControllerIsBuilt();
        if (scanner.hasNext()) {
            serviceManagerServer.stopService(scanner.next());
        } else {
            System.out.println("2nd argument required - service name");
        }
    }

    private static void notifiedStartedByName(Scanner scanner) {
        checkControllerIsBuilt();
        if (scanner.hasNext()) {
            serviceManagerServer.serviceStarted(scanner.next());
        } else {
            System.out.println("2nd argument required - service name");
        }
    }

    private static void notifiedStoppedByName(Scanner scanner) {
        checkControllerIsBuilt();
        if (scanner.hasNext()) {
            serviceManagerServer.serviceStopped(scanner.next());
        } else {
            System.out.println("2nd argument required - service name");
        }
    }

    private static void auditOn(boolean flag) {
        Logger restClientLogger = (Logger) LoggerFactory.getLogger("fluxtion.eventLog");
        if (flag) {
            restClientLogger.setLevel(Level.INFO);
        } else {
            restClientLogger.setLevel(Level.OFF);
        }
    }

    private static final String HANDLER_A = "handlerA";
    private static final String HANDLER_B = "handlerB";
    private static final String HANDLER_C = "handlerC";
    private static final String AGG_AB = "aggAB";
    private static final String CALC_C = "calcC";
    private static final String PERSISTER = "persister";

    private static void buildGraph(boolean compile) {
        if (serviceManagerServer != null) {
            serviceManagerServer.shutdown();
        }
        Service handlerA = Service.builder(HANDLER_A).build();
        Service handlerB = Service.builder(HANDLER_B).build();
        Service handlerC = Service.builder(HANDLER_C).build();
        Service aggAB = Service.builder(AGG_AB)
                .servicesThatRequireMe(List.of(handlerA, handlerB))
                .startTask(CliTestClient::notifyStartedAggAB)
                .build();
        Service calcC = Service.builder(CALC_C)
                .servicesThatRequireMe(List.of(handlerC))
                .build();
        Service persister = Service.builder(PERSISTER)
                .servicesThatRequireMe(List.of(aggAB, calcC))
                .startTask(CliTestClient::notifyStartedPersister)
                .build();

        if (compile) {
            serviceManagerServer = ServiceManager.compiledServiceManager(persister, aggAB, calcC, handlerA, handlerB, handlerC);
        } else {
            serviceManagerServer = ServiceManager.interpretedServiceManager(persister, aggAB, calcC, handlerA, handlerB, handlerC);
        }
        serviceManagerServer.registerStatusListener(new PublishServiceStatusRecordToLog());
    }

    public static void notifyStartedPersister() {
        log.info("persister::startTask notify persister STARTED");
        serviceManagerServer.serviceStarted(PERSISTER);
    }

    public static void notifyStartedAggAB() {
        log.info("aggAB::startTask notify aggAB STARTED");
        serviceManagerServer.serviceStarted(AGG_AB);
    }

    public static final String asciiArtDAG = "" +
            "    Tree view of model\n" +
            "               \n" +
            "                +-------------+                      +------------+              +-----------+      |\n" +
            "                |             |                      |            |              |           |      |\n" +
            "                |  handler_c  |                      | handler_a  |              | handler_b |      |\n" +
            "                +---+---------+                      +----+-------+              +-----+-----+      |\n" +
            "                    |                                     |                            |            |\n" +
            "                    |    +---------+                      |        +-------+           |            |   DIRECTION OF\n" +
            "                    |    |         |                      |        |       |           |            |   EVENT FLOW\n" +
            "                    +----+ calc_c  |                      +--------+agg_AB +-----------+            |\n" +
            "                         +----+----+                               +---+---+                        |\n" +
            "                              |                                        |                            |\n" +
            "                              |                                        |                            |\n" +
            "                              |                +-----------+           |                            |\n" +
            "                              |                |           |           |                            |\n" +
            "                              +----------------+ persister +-----------+                            |\n" +
            "                                               |           |                                        |\n" +
            "                                               +-----------+                                        v\n" +
            "               \n" +
            "                        ";
}
