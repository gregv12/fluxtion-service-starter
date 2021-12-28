---
title: Monitoring
has_children: false
nav_order: 4
published: true
---
# Monitoring
This section covers the runtime monitoring of the ServiceManager. It can be helpful to understand the current state of the
internal model of the ServiceManager and what decisions it took to make a state change.

# Service states
Each service has a state described [here](controllingServices.md#service-lifecycle), a client app can register a listener
to observe the full set of service states in the system. An update is published by ServiceManager whenever an internal 
change is made to any service state. The listener is a `java.util.funciton.Consumer<List<ServiceStatusRecord>>`

**Registering service state listener with method reference**
```java
serviceManager.registerStatusListener(this::recordServiceStatus);
```

# Audit log
The service manager publishes an audit log using slf4j as the logging interface. The logger name of the audit log: 
`fluxtion.service-starter.eventLog`

**sample audit log**

```yaml
    eventTime: 1640551548618
    logTime: 1640551548618
    groupingId: null
    event: RequestStartAll
    eventToString: {GraphEvent.RequestStartAll()}
    nodeLogs: 
        - handlerA_start: { method: startAllServices, initialStatus: STATUS_UNKNOWN, setStatus: WAITING_FOR_PARENTS_TO_START, markStarting: true}
        - handlerB_start: { method: startAllServices, initialStatus: STATUS_UNKNOWN, setStatus: WAITING_FOR_PARENTS_TO_START, markStarting: true}
        - aggAB_start: { method: startAllServices, markStarting: false}
        - aggAB_start: { method: recalculateStatusForStart}
        - handlerC_start: { method: startAllServices, initialStatus: STATUS_UNKNOWN, setStatus: WAITING_FOR_PARENTS_TO_START, markStarting: true}
        - calcC_start: { method: startAllServices, initialStatus: STATUS_UNKNOWN, setStatus: WAITING_FOR_PARENTS_TO_START, markStarting: true}
        - calcC_start: { method: recalculateStatusForStart, initialStatus: WAITING_FOR_PARENTS_TO_START, setStatus: WAITING_FOR_PARENTS_TO_START}
        - persister_start: { method: startAllServices, markStarting: false}
        - persister_start: { method: recalculateStatusForStart}
        - serviceStatusCache: { method: publishStatus}
        - commandPublisher: { method: publishCommands}
    endTime: 1640551548618
---
    eventTime: 1640551548618
    logTime: 1640551548618
    groupingId: null
    event: PublishStartTask
    eventToString: {GraphEvent.PublishStartTask()}
    nodeLogs: 
        - persister_stop: { method: publishStartTasks}
        - aggAB_stop: { method: publishStartTasks}
        - calcC_stop: { method: publishStartTasks, initialStatus: WAITING_FOR_PARENTS_TO_START, setStatus: STARTING}
        - handlerA_stop: { method: publishStartTasks, initialStatus: WAITING_FOR_PARENTS_TO_START, setStatus: STARTING}
        - handlerB_stop: { method: publishStartTasks, initialStatus: WAITING_FOR_PARENTS_TO_START, setStatus: STARTING}
        - handlerC_stop: { method: publishStartTasks}
        - commandPublisher: { method: publishCommands}
    endTime: 1640551548618 
---
    eventTime: 1640551548618
    logTime: 1640551548618
    groupingId: null
    event: PublishStatus
    eventToString: {GraphEvent.PublishStatus()}
    nodeLogs: 
        - serviceStatusCache: { method: publishCurrentStatus}
        - commandPublisher: { method: publishCommands}
    endTime: 1640551548618
---

```


