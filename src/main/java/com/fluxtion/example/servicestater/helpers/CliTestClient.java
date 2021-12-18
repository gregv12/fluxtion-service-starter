package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceManagerServer;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.util.Locale;
import java.util.Scanner;

/**
 * A command line client that tests a sample service graph loaded into the {@link ServiceManager}
 *
 * Various cli commands are provided to exercise all the operations on the service manager. Run the program and a help
 * message is displayed detailing the usage.
 *
 */
@Log
public class CliTestClient {

    private static ServiceManagerServer serviceManagerServer;
    private static ServiceTaskExecutor serviceTaskExecutor;

    public static void main(String[] args) {
        buildGraph();
        serviceManagerServer.startService("aggAB");
        Scanner scanner = new Scanner(System.in);
        boolean run = true;
        printHelp();
        while (run) {
            System.out.print(">");
            String command = scanner.next().toLowerCase(Locale.ROOT);
            switch (command) {
                case "build", "b" -> buildGraph();
                case "status", "ss" -> printStatus();
                case "startall", "sa" -> startAll();
                case "stopall", "ha" -> stopAll();
                case "start", "s" -> startByName(scanner);
                case "stop", "h" -> stopByName(scanner);
                case "ns" -> notifiedStartedByName(scanner);
                case "nh" -> notifiedStoppedByName(scanner);
                case "exit", "e" -> run = false;
                case "help", "?" -> printHelp();
                default -> System.out.println("unknown command:" + command + " ? for command list");
            }
            scanner.nextLine();
        }
        scanner.close();
        serviceTaskExecutor.shutDown();
    }

    static void printHelp() {
        String help = """
                Welcome to FluxtionService interactive tester
                =========================================
                Commands available are:
                help or ?                 - print this message
                build or b                - drops the graph and builds a new graph from scratch
                status or ss              - prints the current status of the graph to console
                startAll or sa            - start all services
                stopAll or ha             - stop all services
                start or s [service name] - start a single services by name
                stop or h [service name]  - stop a single service by name
                ns [service name]         - notify of started status for a single service by name
                hs [service name]         - notify of stopped status for a single service by name
                exit or e                 - exit the application
                """
                ;
        System.out.println(help);
    }

    public static void startAll(){
        checkControllerIsBuilt();
        serviceManagerServer.startAllServices();
    }

    public static void stopAll(){
        checkControllerIsBuilt();
        serviceManagerServer.stopAllServices();
    }

    private static void printStatus(){
        checkControllerIsBuilt();
        serviceManagerServer.publishAllServiceStatus();
    }

    private static void checkControllerIsBuilt() {
        if(serviceManagerServer ==null){
            System.out.println("no service manager built, building one first");
            buildGraph();
        }
    }

    private static void startByName(Scanner scanner){
        checkControllerIsBuilt();
        if(scanner.hasNext()){
            serviceManagerServer.startService(scanner.next());
        }else{
            System.out.println("2nd argument required - service name");
        }
    }

    private static void stopByName(Scanner scanner){
        checkControllerIsBuilt();
        if(scanner.hasNext()){
            serviceManagerServer.stopService(scanner.next());
        }else{
            System.out.println("2nd argument required - service name");
        }
    }

    private static void notifiedStartedByName(Scanner scanner){
        checkControllerIsBuilt();
        if(scanner.hasNext()){
            serviceManagerServer.processServiceStartedNotification(scanner.next());
        }else{
            System.out.println("2nd argument required - service name");
        }
    }

    private static void notifiedStoppedByName(Scanner scanner){
        checkControllerIsBuilt();
        if(scanner.hasNext()){
            serviceManagerServer.processServiceStoppedNotification(scanner.next());
        }else{
            System.out.println("2nd argument required - service name");
        }
    }

    private static void buildGraph() {
        Service handlerA = new Service("handlerA");
        Service handlerB = new Service("handlerB");
        Service handlerC = new Service("handlerC");
        Service aggAB = new Service("aggAB", CliTestClient::notifyStartedAggAB, null, handlerA, handlerB);
        Service calcC = new Service("calcC", handlerC);
        Service persister = new Service("persister", CliTestClient::notifyStartedPersister, null, aggAB, calcC);
        //build and register outputs
        ServiceManager serviceManager = new ServiceManager();
        serviceTaskExecutor = new ServiceTaskExecutor();
        serviceManager.buildServiceController(persister, aggAB, calcC, handlerA, handlerB, handlerC);
        serviceManager.registerTaskExecutor(serviceTaskExecutor);
        serviceManager.registerStatusListener(new PublishStatusToConsole());
        //wrap in server
        serviceManagerServer = new ServiceManagerServer();
        serviceManagerServer.setManager(serviceManager);
    }

    @SneakyThrows
    public static void notifyStart(){
        System.out.println("svc_1 START notification in 4 seconds");
        Thread.sleep(4_000);
        System.out.println("svc_1  sending START notification");
        serviceManagerServer.processServiceStartedNotification("svc_1");
    }

    @SneakyThrows
    public static void notifyStartedPersister(){
        log.info("persister::startTask notify persister STARTED");
        serviceManagerServer.processServiceStartedNotification("persister");
    }

    @SneakyThrows
    public static void notifyStartedAggAB(){
        log.info("aggAB::startTask notify aggAB STARTED");
        serviceManagerServer.processServiceStartedNotification("aggAB");
    }
}
