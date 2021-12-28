---
title: Controlling services
has_children: false
nav_order: 3
published: true
---
# Controlling services
This section covers the programming model and concepts a developer needs to understand when controlling running services
through the ServiceManager. Once a ServiceManager is built clients invoke methods on the built instance to:
- Start/stop individual services
- Start/stop all services
- Notify when a service has stopped or started
- Add a task executor
- Add a status listener

It is important to understand that start and stop tasks work through the service graph in opposite directions. Starting
works from the most downstream service to the highest upstream services. Stopping works from the highest upstream service
to the furthest downstream service.

# Starting services
Clients can start an individual service or all services known to the ServiceManager. ServiceManager inspects the graph
and starts the services that are furthest downstream from the target start service. The services started move up towards
the target start service as start notifications are received from the client application. 

**Starting a single service**
```java
svcManager.startService("svc_2");
```

**Starting all services**

Any service that has no dependencies is marked as a target start service for starting by the ServiceManager
```java
serviceManager.startAllServices();
```

ServiceManager collects start tasks from all services that can be started immediately and publishes the task list to the
TaskExecutor. Dependent services are marked WAITING_FOR_PARENTS_TO_START. When the client app notifies ServiceManager
services have STARTED, new task lists are published for execution. This continues until all services marked
WAITING_FOR_PARENTS_TO_START have started, or until a stop call is issued.

# Stopping services
Clients can stop an individual service or all services known to the ServiceManager. ServiceManager inspects the graph
and stops the services that are furthest upstream from the target stop service. The services stopped move down towards
the target stop service as stop notifications are received from the client application.

**Stopping a single service**
```java
svcManager.stopService("svc_2");
```

**Stopping all services**

Any service that has no dependencies is marked as a target stop service for stopping by the ServiceManager
```java
serviceManager.stopAllServices();
```

ServiceManager collects stop tasks from all services that can be started immediately and publishes the task list to the
TaskExecutor. Dependent services are marked WAITING_FOR_PARENTS_TO_STOP. When the client app notifies ServiceManager
services have STOPPED, new task lists are published for execution. This continues until all services marked
WAITING_FOR_PARENTS_TO_STOP have started, or until a stop call is issued.


# Client feedback
A service within a ServiceManager is only a representation of an external service. The client application must feed back 
the initial state of services and when a service move between STOPPED and STARTED states. The ServiceManager needs 
these updates to know when new tasks lists can be published to the TaskExecutor for execution.

**Notifying started**
```java
svcManager.serviceStarted("svc_2");
```

**Notifying stopped**
```java
svcManager.serviceStopped("svc_2");
```

# Service lifecycle

The service starter manages a set of services with the following behaviour
1. A service can be started if all its dependencies are in a STARTED state
2. A service can be started if it has no dependencies
3. A call to `ServiceManager.startService(String serviceName)` will start any services connected to the named service and that have no dependencies
4. Any service the ServiceManager starts will move to the STARTING state. If a service has a start task it will be published to the TaskExecutor
5. Any dependents of a STARTING service will move to WAITING_FOR_PARENTS_TO_START state
6. A service moves to STARTED state when a client application calls `svcManager.serviceStarted(String serviceName)`
7. When all dependencies of a WAITING_FOR_PARENTS_TO_START service have STARTED state this service will be started, see (4) above
8. Continues up the dependency tree until the target service(s) are started

Stopping has the same behaviour but for the reverse topological order.

## Service states

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
An [AsynchronousTaskExecutor](https://github.com/gregv12/fluxtion-service-starter/blob/master/src/main/java/com/fluxtion/example/servicestater/helpers/AsynchronousTaskExecutor.java)
can be registered with an instance of the ServiceManager:

```java
svcManager.registerTaskExecutor(new AsynchronousTaskExecutor());
```
Tasks are executed asynchronously and in parallel using a thread pool. ServiceManager only issues tasks that can be executed
safely in parallel.