# Fluxtion service starter
An example using [Fluxtion](https://github.com/v12technology/fluxtion) as a dependency controller for starting and stopping services in a deterministic order

The service starter manages a set of services with the following behaviour
1. A service can be started if all its dependencies are in a STARTED state
2. A service can be started if it has no dependencies
3. A call to FluxtionSystemManager.start() will start any services that have no dependencies
4. Any service the FluxtionSystemManager starts will move to the STARTING state, and a [start command](https://github.com/gregv12/example-service-starter/blob/d15d4856af4f0315d08474de5fda74f849886757/src/main/java/com/fluxtion/example/servicestater/ServiceEvent.java#L57) will be published
5. Any dependencies of a STARTING service will move to WAITING_FOR_PARENTS_TO_START state
6. A service moves to STARTED state when a StatusUpdate with STARTED state is invoked by calling FluxtionSystemManager.processStatusUpdate() 
7. When all dependencies of a WAITING_FOR_PARENTS_TO_START service have STARTED state this service will be started, see (4) above
9. Continues down the dependency tree until all services are started

Stopping has the same behaviour for the reverse dependency order.

### States
    STATUS_UNKNOWN,
    WAITING_FOR_PARENTS_TO_START,
    STARTING,
    STARTED,
    WAITING_FOR_PARENTS_TO_STOP,
    STOPPING,
    STOPPED,
    
The main class for client code to use is [FluxtionSystemManager](https://github.com/gregv12/example-service-starter/blob/master/src/main/java/com/fluxtion/example/servicestater/FluxtionSystemManager.java)  

### Service modelling
A service is modelled with [Service](https://github.com/gregv12/example-service-starter/blob/master/src/main/java/com/fluxtion/example/servicestater/Service.java):
- A service has a unique name
- A service has a set of dependencies that must be in STARTED state before this service can start
- Services are controlled in the graph be StartServiceController and StopServiceController
- The controllers process events from the outside world, manage state and create commands to execute on services
- Any commands generated by controllers in a graph cycle are collected in the CommandPublisher
- The CommandPublisher will send to the list of commands to a registered listener for execution 

### Communicating with the outside world
The FluxtionSystemManage reacts to inputs and produces commands for the external system to issue to services. 
A client application registers a command listener and can execute the commands received, example registering a dump commands to console:

```fluxtionServiceManager.registerCommandPublisher(new PublishCommandsToConsole());```

Service status updates listeners are registered in a similar manner:

```fluxtionServiceManager.registerStatusListener(new PublishStatusToConsole());```

## Example

```Java
   void buildSystemController() {

        //replace with JSON/YAML
        Service svc_1 = new Service("svc_1");
        Service svc_2 = new Service("svc_2", svc_1);
        Service svc_A = new Service("svc_A");
        Service svc_B = new Service("svc_B", svc_A);
        //joined service
        Service svc_2BJoined = new Service("svc_2BJoined", svc_2, svc_B);

        //build and register outputs
        FluxtionSystemManager fluxtionServiceManager = new FluxtionSystemManager();
        fluxtionServiceManager.buildSystemController(svc_1, svc_2, svc_A, svc_B, svc_2BJoined);
        fluxtionServiceManager.registerCommandPublisher(new PublishCommandsToConsole());
        fluxtionServiceManager.registerStatusListener(new PublishStatusToConsole());

        //start the service manager
        fluxtionServiceManager.startServices();

        //interact with the service
        fluxtionServiceManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_1"));
        fluxtionServiceManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_2"));
        fluxtionServiceManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_A"));
        fluxtionServiceManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_B"));
        //status query
        fluxtionServiceManager.publishAllServiceStatus();

        fluxtionServiceManager.stopServices();
    }
```


See an image of the service dependencies [here](https://github.com/gregv12/example-service-starter/blob/master/src/main/resources/com/fluxtion/example/servicestater/fluxtionsystemmanager/servicestarter/Processor.png), 
output for sample:

```
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToLog accept
INFO: Current status:
svc_2BJoined - STATUS_UNKNOWN
svc_2 - STATUS_UNKNOWN
svc_B - STATUS_UNKNOWN
svc_1 - STATUS_UNKNOWN
svc_A - STATUS_UNKNOWN


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.graph.FluxtionServiceManager startServices
INFO: start all ServiceEvent.Start(super=name='all')
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.ServiceTaskExecutor accept
INFO: Command list:
ServiceEvent.Start(super=name='svc_1')
ServiceEvent.Start(super=name='svc_A')


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToLog accept
INFO: Current status:
svc_2BJoined - WAITING_FOR_PARENTS_TO_START
svc_2 - WAITING_FOR_PARENTS_TO_START
svc_B - WAITING_FOR_PARENTS_TO_START
svc_1 - STARTING
svc_A - STARTING


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.graph.FluxtionServiceManager processStatusUpdate
INFO: ServiceEvent.StatusUpdate(status=STARTED, name=svc_1)
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToLog accept
INFO: Current status:
svc_2BJoined - WAITING_FOR_PARENTS_TO_START
svc_2 - STARTING
svc_B - WAITING_FOR_PARENTS_TO_START
svc_1 - STARTED
svc_A - STARTING
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.ServiceTaskExecutor accept
INFO: Command list:
ServiceEvent.Start(super=name='svc_2')


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.graph.FluxtionServiceManager processStatusUpdate
INFO: ServiceEvent.StatusUpdate(status=STARTED, name=svc_2)
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToLog accept
INFO: Current status:
svc_2BJoined - WAITING_FOR_PARENTS_TO_START
svc_2 - STARTED
svc_B - WAITING_FOR_PARENTS_TO_START
svc_1 - STARTED
svc_A - STARTING


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.graph.FluxtionServiceManager processStatusUpdate
INFO: ServiceEvent.StatusUpdate(status=STARTED, name=svc_A)
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToLog accept
INFO: Current status:
svc_2BJoined - WAITING_FOR_PARENTS_TO_START
svc_2 - STARTED
svc_B - STARTING
svc_1 - STARTED
svc_A - STARTED
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.ServiceTaskExecutor accept
INFO: Command list:
ServiceEvent.Start(super=name='svc_B')


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.graph.FluxtionServiceManager processStatusUpdate
INFO: ServiceEvent.StatusUpdate(status=STARTED, name=svc_B)
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToLog accept
INFO: Current status:
svc_2BJoined - STARTING
svc_2 - STARTED
svc_B - STARTED
svc_1 - STARTED
svc_A - STARTED
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.ServiceTaskExecutor accept
INFO: Command list:
ServiceEvent.Start(super=name='svc_2BJoined')


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToLog accept
INFO: Current status:
svc_2BJoined - STARTING
svc_2 - STARTED
svc_B - STARTED
svc_1 - STARTED
svc_A - STARTED


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.graph.FluxtionServiceManager stopServices
INFO: start all ServiceEvent.Stop(super=name='all')
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.ServiceTaskExecutor accept
INFO: Command list:
ServiceEvent.Stop(super=name='svc_2BJoined')
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToLog accept
INFO: Current status:
svc_2BJoined - STOPPING
svc_2 - WAITING_FOR_PARENTS_TO_STOP
svc_B - WAITING_FOR_PARENTS_TO_STOP
svc_1 - WAITING_FOR_PARENTS_TO_STOP
svc_A - WAITING_FOR_PARENTS_TO_STOP

```

