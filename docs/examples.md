---
title: Examples
has_children: false
nav_order: 4
published: true
---
#Examples

### Example Cli test client
To help understand the behaviour of the service controller a cli example has been created. The example builds a predetermined
service model and exposes that model as cli application for a user to experiment with, steps:

-  Navigate to [CliTestClient](https://github.com/gregv12/example-service-starter/blob/master/src/main/java/com/fluxtion/example/servicestater/helpers/CliTestClient.java)
-  Execute the main method in CliTestClient. A model will be built at startup and exposed through a cli, that executes methods
   the ServiceManagerServer.

Extract from a sample session, a start task is attached to the persister service that will callback into the server and
indicate the persister service has started:
```

?

FluxtionService interactive tester commands:
===============================================
help or ?                 - print this message
build or b                - drops the graph and builds a new graph from scratch
status or ss              - prints the current status of the graph to console
startAll or sa            - start all services
stopAll or ha             - stop all services
start or s [service name] - start a single services by name
stop or h [service name]  - stop a single service by name
ns [service name]         - notify of started status for a single service by name
nh [service name]         - notify of stopped status for a single service by name
auditOn or aon            - turn audit recording on
auditOff or aoff          - turn audit recording on
exit or e                 - exit the application

>ss
>16:18:50.729 [serviceManagerThread-3] INFO  c.f.e.s.h.PublishServiceStatusRecordToLog - Current status:
(service='aggAB', status=STATUS_UNKNOWN)
(service='handlerC', status=STATUS_UNKNOWN)
(service='handlerB', status=STATUS_UNKNOWN)
(service='handlerA', status=STATUS_UNKNOWN)
(service='persister', status=STATUS_UNKNOWN)
(service='calcC', status=STATUS_UNKNOWN)
16:18:50.729 [serviceManagerThread-3] INFO  fluxtion.eventLog - eventLogRecord: 
    eventTime: 1639930730729
    logTime: 1639930730729
    groupingId: null
    event: PublishStatus
    eventToString: {GraphEvent.PublishStatus()}
    nodeLogs: 
        - serviceStatusCache: { method: publishCurrentStatus}
        - commandPublisher: { method: publishCommands}
    endTime: 1639930730729
16:18:50.729 [serviceManagerThread-3] INFO  fluxtion.eventLog - 
---

s persister
>16:19:05.643 [serviceManagerThread-3] INFO  c.f.e.s.graph.FluxtionServiceManager - start single service:persister
16:19:05.643 [serviceManagerThread-3] INFO  c.f.e.s.h.PublishServiceStatusRecordToLog - Current status:
(service='aggAB', status=STATUS_UNKNOWN)
(service='handlerC', status=STATUS_UNKNOWN)
(service='handlerB', status=STATUS_UNKNOWN)
(service='handlerA', status=STATUS_UNKNOWN)
(service='persister', status=WAITING_FOR_PARENTS_TO_START)
(service='calcC', status=STATUS_UNKNOWN)
16:19:05.643 [serviceManagerThread-3] INFO  fluxtion.eventLog - eventLogRecord: 
    eventTime: -1
    logTime: 1639930745643
    groupingId: null
    event: RequestServiceStart
    eventToString: {GraphEvent.RequestServiceStart(super=name='persister')}
    eventFilter: persister
    nodeLogs: 
        - persister_start: { method: startThisService, initialStatus: STATUS_UNKNOWN, setStatus: WAITING_FOR_PARENTS_TO_START, markStarting: true}
        - serviceStatusCache: { method: publishStatus}
        - commandPublisher: { method: publishCommands}
    endTime: 1639930745643
16:19:05.643 [serviceManagerThread-3] INFO  fluxtion.eventLog - 
---

16:19:05.643 [serviceManagerThread-3] INFO  fluxtion.eventLog - eventLogRecord: 
    eventTime: 1639930745643
    logTime: 1639930745643
    groupingId: null
    event: PublishStartTask
    eventToString: {GraphEvent.PublishStartTask()}
    nodeLogs: 
        - persister_stop: { method: publishStartTasks, initialStatus: WAITING_FOR_PARENTS_TO_START, setStatus: STARTING}
        - aggAB_stop: { method: publishStartTasks}
        - calcC_stop: { method: publishStartTasks}
        - handlerA_stop: { method: publishStartTasks}
        - handlerB_stop: { method: publishStartTasks}
        - handlerC_stop: { method: publishStartTasks}
        - commandPublisher: { method: publishCommands}
    endTime: 1639930745643
16:19:05.643 [serviceManagerThread-3] INFO  fluxtion.eventLog - 
---

16:19:05.643 [serviceManagerThread-3] INFO  c.f.e.s.h.PublishServiceStatusRecordToLog - Current status:
(service='aggAB', status=STATUS_UNKNOWN)
(service='handlerC', status=STATUS_UNKNOWN)
(service='handlerB', status=STATUS_UNKNOWN)
(service='handlerA', status=STATUS_UNKNOWN)
(service='persister', status=STARTING)
(service='calcC', status=STATUS_UNKNOWN)
16:19:05.643 [serviceManagerThread-3] INFO  fluxtion.eventLog - eventLogRecord: 
    eventTime: 1639930745643
    logTime: 1639930745643
    groupingId: null
    event: PublishStatus
    eventToString: {GraphEvent.PublishStatus()}
    nodeLogs: 
        - serviceStatusCache: { method: publishCurrentStatus}
        - commandPublisher: { method: publishCommands}
    endTime: 1639930745643
16:19:05.643 [serviceManagerThread-3] INFO  fluxtion.eventLog - 
---

16:19:05.643 [taskExecutor-2] INFO  c.f.e.s.helpers.ServiceTaskExecutor - executing TaskWrapper{serviceName='persister'startTask='true'}
16:19:05.643 [taskExecutor-2] INFO  c.f.e.s.helpers.CliTestClient - persister::startTask notify persister STARTED
16:19:05.659 [serviceManagerThread-3] INFO  c.f.e.s.graph.FluxtionServiceManager - GraphEvent.NotifyServiceStarted(super=name='persister')
16:19:05.659 [serviceManagerThread-3] INFO  c.f.e.s.h.PublishServiceStatusRecordToLog - Current status:
(service='aggAB', status=STATUS_UNKNOWN)
(service='handlerC', status=STATUS_UNKNOWN)
(service='handlerB', status=STATUS_UNKNOWN)
(service='handlerA', status=STATUS_UNKNOWN)
(service='persister', status=STARTED)
(service='calcC', status=STATUS_UNKNOWN)
16:19:05.659 [serviceManagerThread-3] INFO  fluxtion.eventLog - eventLogRecord: 
    eventTime: -1
    logTime: 1639930745659
    groupingId: null
    event: NotifyServiceStarted
    eventToString: {GraphEvent.NotifyServiceStarted(super=name='persister')}
    eventFilter: persister
    nodeLogs: 
        - persister_stop: { method: notifyServiceStarted, initialStatus: STARTING, setStatus: STARTED}
        - aggAB_stop: { method: recalculateStatusForStop}
        - calcC_stop: { method: recalculateStatusForStop}
        - serviceStatusCache: { method: publishStatus}
        - commandPublisher: { method: publishCommands}
    endTime: 1639930745659
16:19:05.659 [serviceManagerThread-3] INFO  fluxtion.eventLog - 
---
```

