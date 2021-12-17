# Fluxtion service starter
An example using Fluxtion as a dependency controller for starting and stopping services in a deterministic order

The service starter manages a set of services with the following behaviour
1. A service cannot be started until its dependencies are in a STARTED state
1. A call to start will issue a start command any services that do not have dependencies or all dependencies have started and move the service to the STARTING state
1. Any dependencies of a STARTING service will move to WAITING_FOR_PARENTS_TO_START start
1. When all dependencies of a WAITING_FOR_PARENTS_TO_START service have started back to (2) for this service
1. Continues down the dependency tree until all services are started

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

### Communicating with the outside world
The FluxtionSystemManage reacts to inputs and produces commands for the external system to issue to services. A client application registers a command listener and then sends commands to services, example registering a dump commands to console:

```fluxtionSystemManager.registerCommandPublisher(new PublishCommandsToConsole());```

Service status updates listeners are registered in a similar manner:

```fluxtionSystemManager.registerStatusListener(new PublishStatusToConsole());```

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
        FluxtionSystemManager fluxtionSystemManager = new FluxtionSystemManager();
        fluxtionSystemManager.buildSystemController(svc_1, svc_2, svc_A, svc_B, svc_2BJoined);
        fluxtionSystemManager.registerCommandPublisher(new PublishCommandsToConsole());
        fluxtionSystemManager.registerStatusListener(new PublishStatusToConsole());

        //start the service manager
        fluxtionSystemManager.startServices();

        //interact with the service
        fluxtionSystemManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_1"));
        fluxtionSystemManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_2"));
        fluxtionSystemManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_A"));
        fluxtionSystemManager.processStatusUpdate(ServiceEvent.newStartedUpdate( "svc_B"));
        //status query
        fluxtionSystemManager.publishAllServiceStatus();

        fluxtionSystemManager.stopServices();
    }
```


See an image of the service dependencies [here](https://github.com/gregv12/example-service-starter/blob/master/src/main/resources/com/fluxtion/example/servicestater/fluxtionsystemmanager/servicestarter/Processor.png), output for sample:

```
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToConsole accept
INFO: Current status:
svc_2BJoined - STATUS_UNKNOWN
svc_2 - STATUS_UNKNOWN
svc_B - STATUS_UNKNOWN
svc_1 - STATUS_UNKNOWN
svc_A - STATUS_UNKNOWN


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.FluxtionSystemManager startServices
INFO: start all ServiceEvent.Start(super=name='all')


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishCommandsToConsole accept
INFO: Command list:
ServiceEvent.Start(super=name='svc_1')
ServiceEvent.Start(super=name='svc_A')


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToConsole accept
INFO: Current status:
svc_2BJoined - WAITING_FOR_PARENTS_TO_START
svc_2 - WAITING_FOR_PARENTS_TO_START
svc_B - WAITING_FOR_PARENTS_TO_START
svc_1 - STARTING
svc_A - STARTING


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.FluxtionSystemManager processStatusUpdate
INFO: ServiceEvent.StatusUpdate(status=STARTED, name=svc_1)
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToConsole accept
INFO: Current status:
svc_2BJoined - WAITING_FOR_PARENTS_TO_START
svc_2 - STARTING
svc_B - WAITING_FOR_PARENTS_TO_START
svc_1 - STARTED
svc_A - STARTING


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishCommandsToConsole accept
INFO: Command list:
ServiceEvent.Start(super=name='svc_2')
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.FluxtionSystemManager processStatusUpdate
INFO: ServiceEvent.StatusUpdate(status=STARTED, name=svc_2)
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToConsole accept
INFO: Current status:
svc_2BJoined - WAITING_FOR_PARENTS_TO_START
svc_2 - STARTED
svc_B - WAITING_FOR_PARENTS_TO_START
svc_1 - STARTED
svc_A - STARTING


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.FluxtionSystemManager processStatusUpdate
INFO: ServiceEvent.StatusUpdate(status=STARTED, name=svc_A)
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToConsole accept
INFO: Current status:
svc_2BJoined - WAITING_FOR_PARENTS_TO_START
svc_2 - STARTED
svc_B - STARTING
svc_1 - STARTED
svc_A - STARTED


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishCommandsToConsole accept
INFO: Command list:
ServiceEvent.Start(super=name='svc_B')
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.FluxtionSystemManager processStatusUpdate
INFO: ServiceEvent.StatusUpdate(status=STARTED, name=svc_B)
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToConsole accept
INFO: Current status:
svc_2BJoined - STARTING
svc_2 - STARTED
svc_B - STARTED
svc_1 - STARTED
svc_A - STARTED


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishCommandsToConsole accept
INFO: Command list:
ServiceEvent.Start(super=name='svc_2BJoined')


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToConsole accept
INFO: Current status:
svc_2BJoined - STARTING
svc_2 - STARTED
svc_B - STARTED
svc_1 - STARTED
svc_A - STARTED


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.FluxtionSystemManager stopServices
INFO: start all ServiceEvent.Stop(super=name='all')
Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishCommandsToConsole accept
INFO: Command list:
ServiceEvent.Stop(super=name='svc_2BJoined')


Dec 16, 2021 11:34:08 PM com.fluxtion.example.servicestater.helpers.PublishStatusToConsole accept
INFO: Current status:
svc_2BJoined - STOPPING
svc_2 - WAITING_FOR_PARENTS_TO_STOP
svc_B - WAITING_FOR_PARENTS_TO_STOP
svc_1 - WAITING_FOR_PARENTS_TO_STOP
svc_A - WAITING_FOR_PARENTS_TO_STOP

```

