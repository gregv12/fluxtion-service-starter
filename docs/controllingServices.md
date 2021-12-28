---
title: Controlling services
has_children: false
nav_order: 3
published: true
---
# Controlling services
This section covers the programming model and concepts a developer needs to understand when controlling running services
through the ServiceManager. Once a ServiceManager is built clients interact with this instance to:
- Start/stop individual services
- Start/stop all services
- Notify when a service has stopped or started
- Add a task executor
- Add a status listener

It is important to understand that start and stop tasks work through the service graph in opposite directions. Starting
works from the most downstream service to the highest upstream services. Stopping works from the highest upstream service
to the furthest downstream service.

# Service control

## Service lifecycle

The service starter manages a set of services with the following behaviour
1. A service can be started if all its dependencies are in a STARTED state
2. A service can be started if it has no dependencies
3. A call to `ServiceManager.startService(String serviceName)` will start any services that have no dependencies
4. Any service the ServiceManager starts will move to the STARTING state, and a [start command](https://github.com/gregv12/example-service-starter/blob/d15d4856af4f0315d08474de5fda74f849886757/src/main/java/com/fluxtion/example/servicestater/ServiceEvent.java#L57) will be published
5. Any dependencies of a STARTING service will move to WAITING_FOR_PARENTS_TO_START state
6. A service moves to STARTED state when a StatusUpdate with STARTED state is invoked by calling FluxtionSystemManager.processStatusUpdate()
7. When all dependencies of a WAITING_FOR_PARENTS_TO_START service have STARTED state this service will be started, see (4) above
9. Continues down the dependency tree until all services are started

Stopping has the same behaviour for the reverse dependency order.


| Service state                | Notes                                                                                   |
|:-----------------------------|:----------------------------------------------------------------------------------------|
| STATUS_UNKNOWN               | waiting for state notification from client app                                          |
| WAITING_FOR_PARENTS_TO_START | A service dependency is STARTING, move to starting when dependency has STARTED          |
| STARTING                     | start task published by ServiceManager, waiting for status notification from client app |
| STARTED                      | received `ServiceManager.serviceStarted()` from client app                              |
| WAITING_FOR_PARENTS_TO_STOP  | A service dependency is STOPPING, move to starting when dependency has STOPPED          |
| STOPPING                     | stop task published by ServiceManager, waiting for status notification from client app  |
| STOPPED                      | received `ServiceManager.serviceStopped()` from client app                              |


# TaskExecutor
A task list produced by the ServiceManagerServer and pushed to a registered TaskExecutor for execution. The default build
of the ServiceManager uses a [SynchronousTaskExecutor](https://github.com/gregv12/fluxtion-service-starter/blob/master/src/main/java/com/fluxtion/example/servicestater/helpers/SynchronousTaskExecutor.java)
that executes all tasks on the caller's thread.

## Asynchronous task execution
An [ASynchronousTaskExecutor](https://github.com/gregv12/fluxtion-service-starter/blob/master/src/main/java/com/fluxtion/example/servicestater/helpers/ASynchronousTaskExecutor.java)
can be registered with an instance of the ServiceManager:

```java
svcManager.registerTaskExecutor(new ASynchronousTaskExecutor());
```
Tasks are executed asynchronously and in parallel using a thread pool. ServiceManager only issues tasks that can be executed
safely in parallel.