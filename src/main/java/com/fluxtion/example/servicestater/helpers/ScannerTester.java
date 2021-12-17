package com.fluxtion.example.servicestater.helpers;

import com.fluxtion.example.servicestater.FluxtionSystemManager;
import com.fluxtion.example.servicestater.Service;

import java.util.Locale;
import java.util.Scanner;

/**
 * A command line client that tests a sample graph loaded into the
 */
public class ScannerTester {

    private static FluxtionSystemManager fluxtionSystemManager;

    public static void main(String[] args) {
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
                default -> System.out.println("unknown command:" + command);
            }
            scanner.nextLine();
        }
        scanner.close();
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
        fluxtionSystemManager.startAllServices();
    }

    public static void stopAll(){
        checkControllerIsBuilt();
        fluxtionSystemManager.stopAllServices();
    }

    private static void printStatus(){
        checkControllerIsBuilt();
        fluxtionSystemManager.publishAllServiceStatus();
    }

    private static void checkControllerIsBuilt() {
        if(fluxtionSystemManager==null){
            System.out.println("no service manager built, building one first");
            buildGraph();
        }
    }

    private static void startByName(Scanner scanner){
        checkControllerIsBuilt();
        if(scanner.hasNext()){
            fluxtionSystemManager.startService(scanner.next());
        }else{
            System.out.println("2nd argument required - service name");
        }
    }

    private static void stopByName(Scanner scanner){
        checkControllerIsBuilt();
        if(scanner.hasNext()){
            fluxtionSystemManager.stopService(scanner.next());
        }else{
            System.out.println("2nd argument required - service name");
        }
    }

    private static void notifiedStartedByName(Scanner scanner){
        checkControllerIsBuilt();
        if(scanner.hasNext()){
            fluxtionSystemManager.processServiceStartedNotification(scanner.next());
        }else{
            System.out.println("2nd argument required - service name");
        }
    }

    private static void notifiedStoppedByName(Scanner scanner){
        checkControllerIsBuilt();
        if(scanner.hasNext()){
            fluxtionSystemManager.processServiceStoppedNotification(scanner.next());
        }else{
            System.out.println("2nd argument required - service name");
        }
    }

    private static void buildGraph() {
        Service svc_1 = new Service("svc_1", ScannerTester::notifyStart, ScannerTester::notifyStop);
        Service svc_2 = new Service("svc_2", svc_1);
        Service svc_A = new Service("svc_A");
        Service svc_B = new Service("svc_B", svc_A);
        //joined service
        Service svc_2BJoined = new Service("svc_2BJoined", svc_2, svc_B);
        //build and register outputs
        fluxtionSystemManager = new FluxtionSystemManager();
        fluxtionSystemManager.buildSystemController(svc_1, svc_2, svc_A, svc_B, svc_2BJoined);
        fluxtionSystemManager.registerCommandProcessor(new ServiceTaskExecutor());
        fluxtionSystemManager.registerStatusListener(new PublishStatusToConsole());
    }

    public static void notifyStart(){
        System.out.println("svc_1::START task");
    }


    public static void notifyStop(){
        System.out.println("svc_1::STOP task");
    }
}
