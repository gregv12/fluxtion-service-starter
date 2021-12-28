---
title: Building a ServiceManager
has_children: false
nav_order: 2
published: true
---
# Building a ServiceManager
This section covers the programming model and concepts a developer needs to understand when building a ServiceManager.
A ServiceManager is the central class of the service starter, it is the instance client code integrates with after 
building a model. 

A ServiceManager is constructed with a set of services supplied by the client:
```java
ServiceManager serviceManager = ServiceManager.build(serviceProcessOrder, serviceInputOrder);
```

## Service
A service is the atomic unit of control that a ServiceManager operates on, it represents an external service in the 
application. There is a one to one mapping between an external service and a Service definition managed by the 
ServiceManager. Services must have a name that is unique within the ServiceManager instance. 

Services are constructed using the builder pattern:

```java
Service serviceProcessOrder = Service.builder("ServiceProcessOrder").build();
```

Tasks and dependency information is added to the service before calling the build method, see sections below.

## Service model

The ServiceManager builds a model of a Service dependency graph which it uses to control application services at 
runtime. To build a model the ServiceManager requires three pieces of information from the client:

1. What are the services managed? 
2. What is the dependency relationship between services?
3. What tasks should be executed when starting and stopping a service?

### What is the set of Services to be managed?
The ServiceManager exposes static [build methods](//github.com/gregv12/fluxtion-service-starter/blob/master/src/main/java/com/fluxtion/example/servicestater/ServiceManager.java)
which accepts the set of Services to be managed. The ServiceManager will only control Services in this set.

```java
ServiceManager serviceManager = ServiceManager.build(serviceProcessOrder, serviceInputOrder);
```

### What is the dependency relationship between services?
To provide relationship information Service provides two mutators that describe the dependency requirements for
a Service. ServiceManager reads these member variables and builds a model for the whole system. 
- **requiredServices** A service specifies downstream services dependencies
- **servicesThatRequireMe** A downstream service specifies which upstream services require it

Both mutators are optional and give the same information from different perspectives. 

Consider a service `ServiceInputOrder` that requires `ServiceProcessOrder` to be started before starting itself. 
This dependency can be expressed in one of two ways, both of which build the same model. The only difference is 
preference the developer wishes to express the relationship.

- ServiceInputOrder requires ServiceProcessOrder as a dependency 
```java
Service serviceProcessOrder = Service.builder("ServiceProcessOrder")
        .build();
Service serviceInputOrder = Service.builder("ServiceInputOrder")
        .requiredServices(List.of(serviceProcessOrder))
        .build();
```

- ServiceProcessOrder specifies ServiceInputOrder as requiring ServiceProcessOrder
```java
Service serviceInputOrder = Service.builder("ServiceInputOrder")
        .build();
Service serviceProcessOrder = Service.builder("ServiceProcessOrder")
        .servicesThatRequireMe(List.of(serviceInputOrder))
        .build();
```

###  What tasks should be executed when starting and stopping a service?

Tasks are optionally added to Service definition using builder methods. 
- A start task executes when entering the [STARTING](controllingServices.md#service-states) state 
- A stop task executes when the service enters the [STOPPING](controllingServices.md#service-states) state.

Using the `ServiceProcessOrder` example we want to make the service the primary input when it starts, when stopping 
we want to ensure all orders are flushed to downstream systems.

```java
Service serviceProcessOrder = Service.builder("ServiceProcessOrder")
        .startTask(Scratch::makePrimary)
        .stopTask(Scratch::flushAllOrders)
        .build();
```

 
 