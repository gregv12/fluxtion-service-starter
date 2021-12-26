---
title: Build ServerManager
has_children: false
nav_order: 2
published: true
---
This section covers the programming model and concepts a developer needs to understand when building a ServerManager.
A ServerManager is the central class of the service starter, it is the instance client code integrates with after 
building a model. 

## Service 

### Service lifecycle
The service starter manages a set of services with the following behaviour
1. A service can be started if all its dependencies are in a STARTED state
2. A service can be started if it has no dependencies
3. A call to ServiceManager.start() will start any services that have no dependencies
4. Any service the FluxtionSystemManager starts will move to the STARTING state, and a [start command](https://github.com/gregv12/example-service-starter/blob/d15d4856af4f0315d08474de5fda74f849886757/src/main/java/com/fluxtion/example/servicestater/ServiceEvent.java#L57) will be published
5. Any dependencies of a STARTING service will move to WAITING_FOR_PARENTS_TO_START state
6. A service moves to STARTED state when a StatusUpdate with STARTED state is invoked by calling FluxtionSystemManager.processStatusUpdate()
7. When all dependencies of a WAITING_FOR_PARENTS_TO_START service have STARTED state this service will be started, see (4) above
9. Continues down the dependency tree until all services are started

Stopping has the same behaviour for the reverse dependency order.

### Service states
    STATUS_UNKNOWN
    WAITING_FOR_PARENTS_TO_START
    STARTING
    STARTED
    WAITING_FOR_PARENTS_TO_STOP
    STOPPING
    STOPPED
