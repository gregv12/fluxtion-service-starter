/*
 * Copyright (C) 2018 V12 Technology Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Server Side License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program.  If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package com.fluxtion.example.servicestater.graph;

import com.fluxtion.runtime.StaticEventProcessor;
import com.fluxtion.runtime.lifecycle.BatchHandler;
import com.fluxtion.runtime.lifecycle.Lifecycle;
import com.fluxtion.runtime.EventProcessor;

import com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterCommandProcessor;
import com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterStatusListener;
import com.fluxtion.example.servicestater.graph.ForwardPassServiceController;
import com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted;
import com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped;
import com.fluxtion.example.servicestater.graph.GraphEvent.PublishStartTask;
import com.fluxtion.example.servicestater.graph.GraphEvent.PublishStatus;
import com.fluxtion.example.servicestater.graph.GraphEvent.PublishStopTask;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestStartAll;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestStopAll;
import com.fluxtion.example.servicestater.graph.LoadAotCompiledTest;
import com.fluxtion.example.servicestater.graph.ReversePassServiceController;
import com.fluxtion.example.servicestater.graph.ServiceStatusRecordCache;
import com.fluxtion.example.servicestater.graph.TaskWrapperPublisher;
import com.fluxtion.runtime.audit.Auditor;
import com.fluxtion.runtime.audit.EventLogControlEvent;
import com.fluxtion.runtime.audit.EventLogManager;
import com.fluxtion.runtime.event.Event;
import com.fluxtion.runtime.time.Clock;
import com.fluxtion.runtime.time.ClockStrategy.ClockStrategyEvent;
import java.util.Arrays;
import java.util.HashMap;

/*
 * <pre>
 * generation time   : 2022-01-04T06:55:42.914744
 * generator version : 3.1.4
 * api version       : 3.1.4
 * </pre>
 * @author Greg Higgins
 */
@SuppressWarnings({"deprecation", "unchecked", "rawtypes"})
public class Processor implements EventProcessor, StaticEventProcessor, BatchHandler, Lifecycle {

  //Node declarations
  public final Clock clock = new Clock();
  private final TaskWrapperPublisher commandPublisher = new TaskWrapperPublisher();
  public final EventLogManager eventLogger = new EventLogManager(l ->{});
  private final ServiceStatusRecordCache serviceStatusCache = new ServiceStatusRecordCache();
  private final ReversePassServiceController A_stop =
      new ReversePassServiceController("A", commandPublisher, serviceStatusCache);
  private final ForwardPassServiceController B_start =
      new ForwardPassServiceController("B", commandPublisher, serviceStatusCache);
  private final ForwardPassServiceController A_start =
      new ForwardPassServiceController("A", commandPublisher, serviceStatusCache);
  private final ReversePassServiceController B_stop =
      new ReversePassServiceController("B", commandPublisher, serviceStatusCache);
  //Dirty flags
  private boolean isDirty_A_start = false;
  private boolean isDirty_A_stop = false;
  private boolean isDirty_B_start = false;
  private boolean isDirty_B_stop = false;
  private boolean isDirty_clock = false;
  //Filter constants

  public Processor() {
    A_start.setDependents(Arrays.asList(B_start));
    A_start.setStartTask(LoadAotCompiledTest::startA);
    B_start.setDependents(Arrays.asList());
    B_start.setStartTask(LoadAotCompiledTest::startB);
    A_stop.setDependents(Arrays.asList());
    A_stop.setStartTask(LoadAotCompiledTest::startA);
    B_stop.setDependents(Arrays.asList(A_stop));
    B_stop.setStartTask(LoadAotCompiledTest::startB);
    eventLogger.trace = (boolean) true;
    eventLogger.printEventToString = (boolean) true;
    eventLogger.traceLevel = com.fluxtion.runtime.audit.EventLogControlEvent.LogLevel.INFO;
    eventLogger.clock = clock;
    //node auditors
    initialiseAuditor(eventLogger);
    initialiseAuditor(clock);
  }

  @Override
  public void onEvent(Object event) {
    switch (event.getClass().getName()) {
      case ("com.fluxtion.example.servicestater.graph.FluxtionServiceManager$RegisterCommandProcessor"):
        {
          RegisterCommandProcessor typedEvent = (RegisterCommandProcessor) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.FluxtionServiceManager$RegisterStatusListener"):
        {
          RegisterStatusListener typedEvent = (RegisterStatusListener) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$NotifyServiceStarted"):
        {
          NotifyServiceStarted typedEvent = (NotifyServiceStarted) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$NotifyServiceStopped"):
        {
          NotifyServiceStopped typedEvent = (NotifyServiceStopped) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$PublishStartTask"):
        {
          PublishStartTask typedEvent = (PublishStartTask) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$PublishStatus"):
        {
          PublishStatus typedEvent = (PublishStatus) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$PublishStopTask"):
        {
          PublishStopTask typedEvent = (PublishStopTask) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$RequestServiceStart"):
        {
          RequestServiceStart typedEvent = (RequestServiceStart) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$RequestServiceStop"):
        {
          RequestServiceStop typedEvent = (RequestServiceStop) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$RequestStartAll"):
        {
          RequestStartAll typedEvent = (RequestStartAll) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.example.servicestater.graph.GraphEvent$RequestStopAll"):
        {
          RequestStopAll typedEvent = (RequestStopAll) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.runtim.audit.EventLogControlEvent"):
        {
          EventLogControlEvent typedEvent = (EventLogControlEvent) event;
          handleEvent(typedEvent);
          break;
        }
      case ("com.fluxtion.runtim.time.ClockStrategy$ClockStrategyEvent"):
        {
          ClockStrategyEvent typedEvent = (ClockStrategyEvent) event;
          handleEvent(typedEvent);
          break;
        }
    }
  }

  public void handleEvent(RegisterCommandProcessor typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(commandPublisher, "commandPublisher", "registerCommandProcessor", typedEvent);
    commandPublisher.registerCommandProcessor(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(RegisterStatusListener typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(serviceStatusCache, "serviceStatusCache", "registerStatusListener", typedEvent);
    serviceStatusCache.registerStatusListener(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(NotifyServiceStarted typedEvent) {
    auditEvent(typedEvent);
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[A]
      case ("A"):
        auditInvocation(A_stop, "A_stop", "notifyServiceStarted", typedEvent);
        isDirty_A_stop = A_stop.notifyServiceStarted(typedEvent);
        auditInvocation(A_stop, "A_stop", "recalculateStatusForStop", typedEvent);
        isDirty_A_stop = A_stop.recalculateStatusForStop();
        if (isDirty_A_stop) {
          auditInvocation(B_stop, "B_stop", "recalculateStatusForStop", typedEvent);
          isDirty_B_stop = B_stop.recalculateStatusForStop();
        }
        if (isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[B]
      case ("B"):
        auditInvocation(B_stop, "B_stop", "notifyServiceStarted", typedEvent);
        isDirty_B_stop = B_stop.notifyServiceStarted(typedEvent);
        if (isDirty_A_stop) {
          auditInvocation(B_stop, "B_stop", "recalculateStatusForStop", typedEvent);
          isDirty_B_stop = B_stop.recalculateStatusForStop();
        }
        if (isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
    }
    afterEvent();
  }

  public void handleEvent(NotifyServiceStopped typedEvent) {
    auditEvent(typedEvent);
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[A]
      case ("A"):
        auditInvocation(A_start, "A_start", "notifyServiceStopped", typedEvent);
        isDirty_A_start = A_start.notifyServiceStopped(typedEvent);
        if (isDirty_B_start) {
          auditInvocation(A_start, "A_start", "recalculateStatusForStart", typedEvent);
          isDirty_A_start = A_start.recalculateStatusForStart();
        }
        if (isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[B]
      case ("B"):
        auditInvocation(B_start, "B_start", "notifyServiceStopped", typedEvent);
        isDirty_B_start = B_start.notifyServiceStopped(typedEvent);
        auditInvocation(B_start, "B_start", "recalculateStatusForStart", typedEvent);
        isDirty_B_start = B_start.recalculateStatusForStart();
        if (isDirty_B_start) {
          auditInvocation(A_start, "A_start", "recalculateStatusForStart", typedEvent);
          isDirty_A_start = A_start.recalculateStatusForStart();
        }
        if (isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
    }
    afterEvent();
  }

  public void handleEvent(PublishStartTask typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(A_stop, "A_stop", "publishStartTasks", typedEvent);
    isDirty_A_stop = true;
    A_stop.publishStartTasks(typedEvent);
    auditInvocation(B_stop, "B_stop", "publishStartTasks", typedEvent);
    isDirty_B_stop = true;
    B_stop.publishStartTasks(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(PublishStatus typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(serviceStatusCache, "serviceStatusCache", "publishCurrentStatus", typedEvent);
    serviceStatusCache.publishCurrentStatus(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(PublishStopTask typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(B_start, "B_start", "publishStartTasks", typedEvent);
    isDirty_B_start = true;
    B_start.publishStartTasks(typedEvent);
    auditInvocation(A_start, "A_start", "publishStartTasks", typedEvent);
    isDirty_A_start = true;
    A_start.publishStartTasks(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(RequestServiceStart typedEvent) {
    auditEvent(typedEvent);
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[A]
      case ("A"):
        auditInvocation(A_start, "A_start", "startThisService", typedEvent);
        isDirty_A_start = true;
        A_start.startThisService(typedEvent);
        if (isDirty_B_start) {
          auditInvocation(A_start, "A_start", "recalculateStatusForStart", typedEvent);
          isDirty_A_start = true;
          A_start.recalculateStatusForStart();
        }
        if (isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[B]
      case ("B"):
        auditInvocation(B_start, "B_start", "startThisService", typedEvent);
        isDirty_B_start = true;
        B_start.startThisService(typedEvent);
        auditInvocation(B_start, "B_start", "recalculateStatusForStart", typedEvent);
        isDirty_B_start = true;
        B_start.recalculateStatusForStart();
        if (isDirty_B_start) {
          auditInvocation(A_start, "A_start", "recalculateStatusForStart", typedEvent);
          isDirty_A_start = true;
          A_start.recalculateStatusForStart();
        }
        if (isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
    }
    afterEvent();
  }

  public void handleEvent(RequestServiceStop typedEvent) {
    auditEvent(typedEvent);
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[A]
      case ("A"):
        auditInvocation(A_stop, "A_stop", "stopThisService", typedEvent);
        isDirty_A_stop = true;
        A_stop.stopThisService(typedEvent);
        auditInvocation(A_stop, "A_stop", "recalculateStatusForStop", typedEvent);
        isDirty_A_stop = true;
        A_stop.recalculateStatusForStop();
        if (isDirty_A_stop) {
          auditInvocation(B_stop, "B_stop", "recalculateStatusForStop", typedEvent);
          isDirty_B_stop = true;
          B_stop.recalculateStatusForStop();
        }
        if (isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[B]
      case ("B"):
        auditInvocation(B_stop, "B_stop", "stopThisService", typedEvent);
        isDirty_B_stop = true;
        B_stop.stopThisService(typedEvent);
        if (isDirty_A_stop) {
          auditInvocation(B_stop, "B_stop", "recalculateStatusForStop", typedEvent);
          isDirty_B_stop = true;
          B_stop.recalculateStatusForStop();
        }
        if (isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
    }
    afterEvent();
  }

  public void handleEvent(RequestStartAll typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(B_start, "B_start", "startAllServices", typedEvent);
    isDirty_B_start = true;
    B_start.startAllServices(typedEvent);
    auditInvocation(B_start, "B_start", "recalculateStatusForStart", typedEvent);
    isDirty_B_start = true;
    B_start.recalculateStatusForStart();
    auditInvocation(A_start, "A_start", "startAllServices", typedEvent);
    isDirty_A_start = true;
    A_start.startAllServices(typedEvent);
    if (isDirty_B_start) {
      auditInvocation(A_start, "A_start", "recalculateStatusForStart", typedEvent);
      isDirty_A_start = true;
      A_start.recalculateStatusForStart();
    }
    if (isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop) {
      auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
      serviceStatusCache.publishStatus();
    }
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(RequestStopAll typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(A_stop, "A_stop", "stopAllServices", typedEvent);
    isDirty_A_stop = true;
    A_stop.stopAllServices(typedEvent);
    auditInvocation(A_stop, "A_stop", "recalculateStatusForStop", typedEvent);
    isDirty_A_stop = true;
    A_stop.recalculateStatusForStop();
    auditInvocation(B_stop, "B_stop", "stopAllServices", typedEvent);
    isDirty_B_stop = true;
    B_stop.stopAllServices(typedEvent);
    if (isDirty_A_stop) {
      auditInvocation(B_stop, "B_stop", "recalculateStatusForStop", typedEvent);
      isDirty_B_stop = true;
      B_stop.recalculateStatusForStop();
    }
    if (isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop) {
      auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
      serviceStatusCache.publishStatus();
    }
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(EventLogControlEvent typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(eventLogger, "eventLogger", "calculationLogConfig", typedEvent);
    eventLogger.calculationLogConfig(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  public void handleEvent(ClockStrategyEvent typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(clock, "clock", "setClockStrategy", typedEvent);
    isDirty_clock = true;
    clock.setClockStrategy(typedEvent);
    //event stack unwind callbacks
    afterEvent();
  }

  private void auditEvent(Object typedEvent) {
    eventLogger.eventReceived(typedEvent);
    clock.eventReceived(typedEvent);
  }

  private void auditEvent(Event typedEvent) {
    eventLogger.eventReceived(typedEvent);
    clock.eventReceived(typedEvent);
  }

  private void auditInvocation(Object node, String nodeName, String methodName, Object typedEvent) {
    eventLogger.nodeInvoked(node, nodeName, methodName, typedEvent);
  }

  private void initialiseAuditor(Auditor auditor) {
    auditor.init();
    auditor.nodeRegistered(A_start, "A_start");
    auditor.nodeRegistered(B_start, "B_start");
    auditor.nodeRegistered(A_stop, "A_stop");
    auditor.nodeRegistered(B_stop, "B_stop");
    auditor.nodeRegistered(serviceStatusCache, "serviceStatusCache");
    auditor.nodeRegistered(commandPublisher, "commandPublisher");
  }

  private void afterEvent() {
    commandPublisher.publishCommands();
    eventLogger.processingComplete();
    clock.processingComplete();
    isDirty_A_start = false;
    isDirty_A_stop = false;
    isDirty_B_start = false;
    isDirty_B_stop = false;
    isDirty_clock = false;
  }

  @Override
  public void init() {
    clock.init();
    serviceStatusCache.init();
    A_stop.initialise();
    B_start.initialise();
    A_start.initialise();
    B_stop.initialise();
  }

  @Override
  public void tearDown() {
    clock.tearDown();
    eventLogger.tearDown();
  }

  @Override
  public void batchPause() {}

  @Override
  public void batchEnd() {}
}
