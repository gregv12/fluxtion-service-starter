/*
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
*
<http://www.mongodb.com/licensing/server-side-public-license>.
*/
package com.fluxtion.example.servicestater.testgenerated;

import com.fluxtion.runtime.StaticEventProcessor;
import com.fluxtion.runtime.lifecycle.BatchHandler;
import com.fluxtion.runtime.lifecycle.Lifecycle;
import com.fluxtion.runtime.EventProcessor;
import com.fluxtion.runtime.callback.InternalEventProcessor;
import com.fluxtion.example.servicestater.ServiceQuery;
import com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterCommandProcessor;
import com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterStatusListener;
import com.fluxtion.example.servicestater.graph.ForwardPassServiceController;
import com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted;
import com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped;
import com.fluxtion.example.servicestater.graph.GraphEvent.PublishStartTask;
import com.fluxtion.example.servicestater.graph.GraphEvent.PublishStatus;
import com.fluxtion.example.servicestater.graph.GraphEvent.PublishStopTask;
import com.fluxtion.example.servicestater.graph.GraphEvent.RegisterWrappedInstance;
import com.fluxtion.example.servicestater.graph.GraphEvent.RemoveService;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestStartAll;
import com.fluxtion.example.servicestater.graph.GraphEvent.RequestStopAll;
import com.fluxtion.example.servicestater.graph.LoadAotCompiledTest;
import com.fluxtion.example.servicestater.graph.ReversePassServiceController;
import com.fluxtion.example.servicestater.graph.ServiceStatusRecordCache;
import com.fluxtion.example.servicestater.graph.TaskWrapperPublisher;
import com.fluxtion.runtime.EventProcessorContext;
import com.fluxtion.runtime.audit.Auditor;
import com.fluxtion.runtime.audit.EventLogControlEvent;
import com.fluxtion.runtime.audit.EventLogManager;
import com.fluxtion.runtime.audit.NodeNameAuditor;
import com.fluxtion.runtime.callback.CallbackDispatcherImpl;
import com.fluxtion.runtime.callback.ExportFunctionAuditEvent;
import com.fluxtion.runtime.event.Event;
import com.fluxtion.runtime.input.EventFeed;
import com.fluxtion.runtime.input.SubscriptionManagerNode;
import com.fluxtion.runtime.node.ForkedTriggerTask;
import com.fluxtion.runtime.node.MutableEventProcessorContext;
import com.fluxtion.runtime.time.Clock;
import com.fluxtion.runtime.time.ClockStrategy.ClockStrategyEvent;
import java.util.Arrays;
import java.util.Map;

import java.util.IdentityHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 *
 *
 * <pre>
 * generation time                 : Not available
 * eventProcessorGenerator version : 9.3.14
 * api version                     : 9.3.14
 * </pre>
 *
 * Event classes supported:
 *
 * <ul>
 *   <li>com.fluxtion.compiler.generation.model.ExportFunctionMarker
 *   <li>com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterCommandProcessor
 *   <li>com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterStatusListener
 *   <li>com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted
 *   <li>com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped
 *   <li>com.fluxtion.example.servicestater.graph.GraphEvent.PublishStartTask
 *   <li>com.fluxtion.example.servicestater.graph.GraphEvent.PublishStatus
 *   <li>com.fluxtion.example.servicestater.graph.GraphEvent.PublishStopTask
 *   <li>com.fluxtion.example.servicestater.graph.GraphEvent.RegisterWrappedInstance
 *   <li>com.fluxtion.example.servicestater.graph.GraphEvent.RemoveService
 *   <li>com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart
 *   <li>com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop
 *   <li>com.fluxtion.example.servicestater.graph.GraphEvent.RequestStartAll
 *   <li>com.fluxtion.example.servicestater.graph.GraphEvent.RequestStopAll
 *   <li>com.fluxtion.runtime.audit.EventLogControlEvent
 *   <li>com.fluxtion.runtime.time.ClockStrategy.ClockStrategyEvent
 * </ul>
 *
 * @author Greg Higgins
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ProcessorTestLoad
    implements EventProcessor<ProcessorTestLoad>,
        /*--- @ExportService start ---*/
        ServiceQuery,
        /*--- @ExportService end ---*/
        StaticEventProcessor,
        InternalEventProcessor,
        BatchHandler,
        Lifecycle {

  //Node declarations
  private final CallbackDispatcherImpl callbackDispatcher = new CallbackDispatcherImpl();
  public final Clock clock = new Clock();
  private final TaskWrapperPublisher commandPublisher = new TaskWrapperPublisher();
  public final EventLogManager eventLogger = new EventLogManager();
  public final NodeNameAuditor nodeNameLookup = new NodeNameAuditor();
  private final ServiceStatusRecordCache serviceStatusCache = new ServiceStatusRecordCache();
  private final ReversePassServiceController A_stop =
      new ReversePassServiceController("A", commandPublisher, serviceStatusCache);
  private final ForwardPassServiceController B_start =
      new ForwardPassServiceController("B", commandPublisher, serviceStatusCache);
  private final ForwardPassServiceController A_start =
      new ForwardPassServiceController("A", commandPublisher, serviceStatusCache);
  private final ReversePassServiceController B_stop =
      new ReversePassServiceController("B", commandPublisher, serviceStatusCache);
  private final SubscriptionManagerNode subscriptionManager = new SubscriptionManagerNode();
  private final MutableEventProcessorContext context =
      new MutableEventProcessorContext(
          nodeNameLookup, callbackDispatcher, subscriptionManager, callbackDispatcher);
  private final ExportFunctionAuditEvent functionAudit = new ExportFunctionAuditEvent();
  //Dirty flags
  private boolean initCalled = false;
  private boolean processing = false;
  private boolean buffering = false;
  private final IdentityHashMap<Object, BooleanSupplier> dirtyFlagSupplierMap =
      new IdentityHashMap<>(5);
  private final IdentityHashMap<Object, Consumer<Boolean>> dirtyFlagUpdateMap =
      new IdentityHashMap<>(5);

  private boolean isDirty_A_start = false;
  private boolean isDirty_A_stop = false;
  private boolean isDirty_B_start = false;
  private boolean isDirty_B_stop = false;
  private boolean isDirty_clock = false;

  //Forked declarations

  //Filter constants

  //unknown event handler
  private Consumer unKnownEventHandler = (e) -> {};

  public ProcessorTestLoad(Map<Object, Object> contextMap) {
    if (context != null) {
      context.replaceMappings(contextMap);
    }
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
    eventLogger.printThreadName = (boolean) true;
    eventLogger.traceLevel = com.fluxtion.runtime.audit.EventLogControlEvent.LogLevel.INFO;
    eventLogger.clock = clock;
    //node auditors
    initialiseAuditor(clock);
    initialiseAuditor(eventLogger);
    initialiseAuditor(nodeNameLookup);
    if (subscriptionManager != null) {
      subscriptionManager.setSubscribingEventProcessor(this);
    }
    if (context != null) {
      context.setEventProcessorCallback(this);
    }
  }

  public ProcessorTestLoad() {
    this(null);
  }

  @Override
  public void init() {
    initCalled = true;
    auditEvent(Lifecycle.LifecycleEvent.Init);
    //initialise dirty lookup map
    isDirty("test");
    clock.init();
    serviceStatusCache.init();
    A_stop.initialise();
    B_start.initialise();
    A_start.initialise();
    B_stop.initialise();
    afterEvent();
  }

  @Override
  public void start() {
    if (!initCalled) {
      throw new RuntimeException("init() must be called before start()");
    }
    processing = true;
    auditEvent(Lifecycle.LifecycleEvent.Start);

    afterEvent();
    callbackDispatcher.dispatchQueuedCallbacks();
    processing = false;
  }

  @Override
  public void stop() {
    if (!initCalled) {
      throw new RuntimeException("init() must be called before stop()");
    }
    processing = true;
    auditEvent(Lifecycle.LifecycleEvent.Stop);

    afterEvent();
    callbackDispatcher.dispatchQueuedCallbacks();
    processing = false;
  }

  @Override
  public void tearDown() {
    initCalled = false;
    auditEvent(Lifecycle.LifecycleEvent.TearDown);
    nodeNameLookup.tearDown();
    eventLogger.tearDown();
    clock.tearDown();
    subscriptionManager.tearDown();
    afterEvent();
  }

  @Override
  public void setContextParameterMap(Map<Object, Object> newContextMapping) {
    context.replaceMappings(newContextMapping);
  }

  @Override
  public void addContextParameter(Object key, Object value) {
    context.addMapping(key, value);
  }

  //EVENT DISPATCH - START
  @Override
  public void onEvent(Object event) {
    if (buffering) {
      triggerCalculation();
    }
    if (processing) {
      callbackDispatcher.processReentrantEvent(event);
    } else {
      processing = true;
      onEventInternal(event);
      callbackDispatcher.dispatchQueuedCallbacks();
      processing = false;
    }
  }

  @Override
  public void onEventInternal(Object event) {
    if (event
        instanceof
        com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterCommandProcessor) {
      RegisterCommandProcessor typedEvent = (RegisterCommandProcessor) event;
      handleEvent(typedEvent);
    } else if (event
        instanceof
        com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterStatusListener) {
      RegisterStatusListener typedEvent = (RegisterStatusListener) event;
      handleEvent(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted) {
      NotifyServiceStarted typedEvent = (NotifyServiceStarted) event;
      handleEvent(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped) {
      NotifyServiceStopped typedEvent = (NotifyServiceStopped) event;
      handleEvent(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.PublishStartTask) {
      PublishStartTask typedEvent = (PublishStartTask) event;
      handleEvent(typedEvent);
    } else if (event instanceof com.fluxtion.example.servicestater.graph.GraphEvent.PublishStatus) {
      PublishStatus typedEvent = (PublishStatus) event;
      handleEvent(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.PublishStopTask) {
      PublishStopTask typedEvent = (PublishStopTask) event;
      handleEvent(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RegisterWrappedInstance) {
      RegisterWrappedInstance typedEvent = (RegisterWrappedInstance) event;
      handleEvent(typedEvent);
    } else if (event instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RemoveService) {
      RemoveService typedEvent = (RemoveService) event;
      handleEvent(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart) {
      RequestServiceStart typedEvent = (RequestServiceStart) event;
      handleEvent(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop) {
      RequestServiceStop typedEvent = (RequestServiceStop) event;
      handleEvent(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RequestStartAll) {
      RequestStartAll typedEvent = (RequestStartAll) event;
      handleEvent(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RequestStopAll) {
      RequestStopAll typedEvent = (RequestStopAll) event;
      handleEvent(typedEvent);
    } else if (event instanceof com.fluxtion.runtime.audit.EventLogControlEvent) {
      EventLogControlEvent typedEvent = (EventLogControlEvent) event;
      handleEvent(typedEvent);
    } else if (event instanceof com.fluxtion.runtime.time.ClockStrategy.ClockStrategyEvent) {
      ClockStrategyEvent typedEvent = (ClockStrategyEvent) event;
      handleEvent(typedEvent);
    } else {
      unKnownEventHandler(event);
    }
  }

  public void handleEvent(RegisterCommandProcessor typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(commandPublisher, "commandPublisher", "registerCommandProcessor", typedEvent);
    commandPublisher.registerCommandProcessor(typedEvent);
    afterEvent();
  }

  public void handleEvent(RegisterStatusListener typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(serviceStatusCache, "serviceStatusCache", "registerStatusListener", typedEvent);
    serviceStatusCache.registerStatusListener(typedEvent);
    afterEvent();
  }

  public void handleEvent(NotifyServiceStarted typedEvent) {
    auditEvent(typedEvent);
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[A]
      case ("A"):
        auditInvocation(A_stop, "A_stop", "notifyServiceStarted", typedEvent);
        isDirty_A_stop = A_stop.notifyServiceStarted(typedEvent);
        if (guardCheck_B_stop()) {
          auditInvocation(B_stop, "B_stop", "recalculateStatusForStop", typedEvent);
          isDirty_B_stop = B_stop.recalculateStatusForStop();
        }
        if (guardCheck_serviceStatusCache()) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[B]
      case ("B"):
        auditInvocation(B_stop, "B_stop", "notifyServiceStarted", typedEvent);
        isDirty_B_stop = B_stop.notifyServiceStarted(typedEvent);
        if (guardCheck_serviceStatusCache()) {
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
        if (guardCheck_serviceStatusCache()) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[B]
      case ("B"):
        auditInvocation(B_start, "B_start", "notifyServiceStopped", typedEvent);
        isDirty_B_start = B_start.notifyServiceStopped(typedEvent);
        if (guardCheck_A_start()) {
          auditInvocation(A_start, "A_start", "recalculateStatusForStart", typedEvent);
          isDirty_A_start = A_start.recalculateStatusForStart();
        }
        if (guardCheck_serviceStatusCache()) {
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
    isDirty_A_stop = A_stop.publishStartTasks(typedEvent);
    auditInvocation(B_stop, "B_stop", "publishStartTasks", typedEvent);
    isDirty_B_stop = B_stop.publishStartTasks(typedEvent);
    afterEvent();
  }

  public void handleEvent(PublishStatus typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(serviceStatusCache, "serviceStatusCache", "publishCurrentStatus", typedEvent);
    serviceStatusCache.publishCurrentStatus(typedEvent);
    afterEvent();
  }

  public void handleEvent(PublishStopTask typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(B_start, "B_start", "publishStartTasks", typedEvent);
    isDirty_B_start = B_start.publishStartTasks(typedEvent);
    auditInvocation(A_start, "A_start", "publishStartTasks", typedEvent);
    isDirty_A_start = A_start.publishStartTasks(typedEvent);
    afterEvent();
  }

  public void handleEvent(RegisterWrappedInstance typedEvent) {
    auditEvent(typedEvent);
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RegisterWrappedInstance] filterString:[A]
      case ("A"):
        auditInvocation(A_stop, "A_stop", "registerWrappedInstance", typedEvent);
        isDirty_A_stop = A_stop.registerWrappedInstance(typedEvent);
        auditInvocation(A_start, "A_start", "registerWrappedInstance", typedEvent);
        isDirty_A_start = A_start.registerWrappedInstance(typedEvent);
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RegisterWrappedInstance] filterString:[B]
      case ("B"):
        auditInvocation(B_start, "B_start", "registerWrappedInstance", typedEvent);
        isDirty_B_start = B_start.registerWrappedInstance(typedEvent);
        auditInvocation(B_stop, "B_stop", "registerWrappedInstance", typedEvent);
        isDirty_B_stop = B_stop.registerWrappedInstance(typedEvent);
        afterEvent();
        return;
    }
    afterEvent();
  }

  public void handleEvent(RemoveService typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(A_stop, "A_stop", "removeDependent", typedEvent);
    isDirty_A_stop = A_stop.removeDependent(typedEvent);
    auditInvocation(B_start, "B_start", "removeDependent", typedEvent);
    isDirty_B_start = B_start.removeDependent(typedEvent);
    auditInvocation(A_start, "A_start", "removeDependent", typedEvent);
    isDirty_A_start = A_start.removeDependent(typedEvent);
    auditInvocation(B_stop, "B_stop", "removeDependent", typedEvent);
    isDirty_B_stop = B_stop.removeDependent(typedEvent);
    auditInvocation(serviceStatusCache, "serviceStatusCache", "removeDependent", typedEvent);
    serviceStatusCache.removeDependent(typedEvent);
    afterEvent();
  }

  public void handleEvent(RequestServiceStart typedEvent) {
    auditEvent(typedEvent);
    switch (typedEvent.filterString()) {
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[A]
      case ("A"):
        auditInvocation(A_start, "A_start", "startThisService", typedEvent);
        isDirty_A_start = A_start.startThisService(typedEvent);
        if (guardCheck_serviceStatusCache()) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[B]
      case ("B"):
        auditInvocation(B_start, "B_start", "startThisService", typedEvent);
        isDirty_B_start = B_start.startThisService(typedEvent);
        if (guardCheck_A_start()) {
          auditInvocation(A_start, "A_start", "recalculateStatusForStart", typedEvent);
          isDirty_A_start = A_start.recalculateStatusForStart();
        }
        if (guardCheck_serviceStatusCache()) {
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
        isDirty_A_stop = A_stop.stopThisService(typedEvent);
        if (guardCheck_B_stop()) {
          auditInvocation(B_stop, "B_stop", "recalculateStatusForStop", typedEvent);
          isDirty_B_stop = B_stop.recalculateStatusForStop();
        }
        if (guardCheck_serviceStatusCache()) {
          auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
          serviceStatusCache.publishStatus();
        }
        afterEvent();
        return;
        //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[B]
      case ("B"):
        auditInvocation(B_stop, "B_stop", "stopThisService", typedEvent);
        isDirty_B_stop = B_stop.stopThisService(typedEvent);
        if (guardCheck_serviceStatusCache()) {
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
    isDirty_B_start = B_start.startAllServices(typedEvent);
    auditInvocation(A_start, "A_start", "startAllServices", typedEvent);
    isDirty_A_start = A_start.startAllServices(typedEvent);
    if (guardCheck_A_start()) {
      auditInvocation(A_start, "A_start", "recalculateStatusForStart", typedEvent);
      isDirty_A_start = A_start.recalculateStatusForStart();
    }
    if (guardCheck_serviceStatusCache()) {
      auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
      serviceStatusCache.publishStatus();
    }
    afterEvent();
  }

  public void handleEvent(RequestStopAll typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(A_stop, "A_stop", "stopAllServices", typedEvent);
    isDirty_A_stop = A_stop.stopAllServices(typedEvent);
    auditInvocation(B_stop, "B_stop", "stopAllServices", typedEvent);
    isDirty_B_stop = B_stop.stopAllServices(typedEvent);
    if (guardCheck_B_stop()) {
      auditInvocation(B_stop, "B_stop", "recalculateStatusForStop", typedEvent);
      isDirty_B_stop = B_stop.recalculateStatusForStop();
    }
    if (guardCheck_serviceStatusCache()) {
      auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
      serviceStatusCache.publishStatus();
    }
    afterEvent();
  }

  public void handleEvent(EventLogControlEvent typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(eventLogger, "eventLogger", "calculationLogConfig", typedEvent);
    eventLogger.calculationLogConfig(typedEvent);
    afterEvent();
  }

  public void handleEvent(ClockStrategyEvent typedEvent) {
    auditEvent(typedEvent);
    //Default, no filter methods
    auditInvocation(clock, "clock", "setClockStrategy", typedEvent);
    isDirty_clock = true;
    clock.setClockStrategy(typedEvent);
    afterEvent();
  }
  //EVENT DISPATCH - END

  //EXPORTED SERVICE FUNCTIONS - START
  @Override
  public void startOrder(
      java.util.function.Consumer<com.fluxtion.example.servicestater.ServiceOrderRecord<?>> arg0) {
    beforeServiceCall(
        "public void com.fluxtion.example.servicestater.graph.ReversePassServiceController.startOrder(java.util.function.Consumer<com.fluxtion.example.servicestater.ServiceOrderRecord<?>>)");
    ExportFunctionAuditEvent typedEvent = functionAudit;
    auditInvocation(A_stop, "A_stop", "startOrder", typedEvent);
    isDirty_A_stop = true;
    A_stop.startOrder(arg0);
    auditInvocation(B_start, "B_start", "startOrder", typedEvent);
    isDirty_B_start = true;
    B_start.startOrder(arg0);
    auditInvocation(A_start, "A_start", "startOrder", typedEvent);
    isDirty_A_start = true;
    A_start.startOrder(arg0);
    auditInvocation(B_stop, "B_stop", "startOrder", typedEvent);
    isDirty_B_stop = true;
    B_stop.startOrder(arg0);
    afterServiceCall();
  }

  @Override
  public void stopOrder(
      java.util.function.Consumer<com.fluxtion.example.servicestater.ServiceOrderRecord<?>> arg0) {
    beforeServiceCall(
        "public void com.fluxtion.example.servicestater.graph.ServiceController.stopOrder(java.util.function.Consumer<com.fluxtion.example.servicestater.ServiceOrderRecord<?>>)");
    ExportFunctionAuditEvent typedEvent = functionAudit;
    auditInvocation(A_stop, "A_stop", "stopOrder", typedEvent);
    isDirty_A_stop = true;
    A_stop.stopOrder(arg0);
    auditInvocation(B_start, "B_start", "stopOrder", typedEvent);
    isDirty_B_start = true;
    B_start.stopOrder(arg0);
    auditInvocation(A_start, "A_start", "stopOrder", typedEvent);
    isDirty_A_start = true;
    A_start.stopOrder(arg0);
    auditInvocation(B_stop, "B_stop", "stopOrder", typedEvent);
    isDirty_B_stop = true;
    B_stop.stopOrder(arg0);
    afterServiceCall();
  }
  //EXPORTED SERVICE FUNCTIONS - END

  public void bufferEvent(Object event) {
    buffering = true;
    if (event
        instanceof
        com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterCommandProcessor) {
      RegisterCommandProcessor typedEvent = (RegisterCommandProcessor) event;
      auditEvent(typedEvent);
      auditInvocation(commandPublisher, "commandPublisher", "registerCommandProcessor", typedEvent);
      commandPublisher.registerCommandProcessor(typedEvent);
    } else if (event
        instanceof
        com.fluxtion.example.servicestater.graph.FluxtionServiceManager.RegisterStatusListener) {
      RegisterStatusListener typedEvent = (RegisterStatusListener) event;
      auditEvent(typedEvent);
      auditInvocation(
          serviceStatusCache, "serviceStatusCache", "registerStatusListener", typedEvent);
      serviceStatusCache.registerStatusListener(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted) {
      NotifyServiceStarted typedEvent = (NotifyServiceStarted) event;
      auditEvent(typedEvent);
      switch (typedEvent.filterString()) {
          //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[A]
        case ("A"):
          auditInvocation(A_stop, "A_stop", "notifyServiceStarted", typedEvent);
          isDirty_A_stop = true;
          A_stop.notifyServiceStarted(typedEvent);
          afterEvent();
          return;
          //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStarted] filterString:[B]
        case ("B"):
          auditInvocation(B_stop, "B_stop", "notifyServiceStarted", typedEvent);
          isDirty_B_stop = true;
          B_stop.notifyServiceStarted(typedEvent);
          afterEvent();
          return;
      }
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped) {
      NotifyServiceStopped typedEvent = (NotifyServiceStopped) event;
      auditEvent(typedEvent);
      switch (typedEvent.filterString()) {
          //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[A]
        case ("A"):
          auditInvocation(A_start, "A_start", "notifyServiceStopped", typedEvent);
          isDirty_A_start = true;
          A_start.notifyServiceStopped(typedEvent);
          afterEvent();
          return;
          //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.NotifyServiceStopped] filterString:[B]
        case ("B"):
          auditInvocation(B_start, "B_start", "notifyServiceStopped", typedEvent);
          isDirty_B_start = true;
          B_start.notifyServiceStopped(typedEvent);
          afterEvent();
          return;
      }
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.PublishStartTask) {
      PublishStartTask typedEvent = (PublishStartTask) event;
      auditEvent(typedEvent);
      auditInvocation(A_stop, "A_stop", "publishStartTasks", typedEvent);
      isDirty_A_stop = true;
      A_stop.publishStartTasks(typedEvent);
      auditInvocation(B_stop, "B_stop", "publishStartTasks", typedEvent);
      isDirty_B_stop = true;
      B_stop.publishStartTasks(typedEvent);
    } else if (event instanceof com.fluxtion.example.servicestater.graph.GraphEvent.PublishStatus) {
      PublishStatus typedEvent = (PublishStatus) event;
      auditEvent(typedEvent);
      auditInvocation(serviceStatusCache, "serviceStatusCache", "publishCurrentStatus", typedEvent);
      serviceStatusCache.publishCurrentStatus(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.PublishStopTask) {
      PublishStopTask typedEvent = (PublishStopTask) event;
      auditEvent(typedEvent);
      auditInvocation(B_start, "B_start", "publishStartTasks", typedEvent);
      isDirty_B_start = true;
      B_start.publishStartTasks(typedEvent);
      auditInvocation(A_start, "A_start", "publishStartTasks", typedEvent);
      isDirty_A_start = true;
      A_start.publishStartTasks(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RegisterWrappedInstance) {
      RegisterWrappedInstance typedEvent = (RegisterWrappedInstance) event;
      auditEvent(typedEvent);
      switch (typedEvent.filterString()) {
          //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RegisterWrappedInstance] filterString:[A]
        case ("A"):
          auditInvocation(A_stop, "A_stop", "registerWrappedInstance", typedEvent);
          isDirty_A_stop = true;
          A_stop.registerWrappedInstance(typedEvent);
          auditInvocation(A_start, "A_start", "registerWrappedInstance", typedEvent);
          isDirty_A_start = true;
          A_start.registerWrappedInstance(typedEvent);
          afterEvent();
          return;
          //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RegisterWrappedInstance] filterString:[B]
        case ("B"):
          auditInvocation(B_start, "B_start", "registerWrappedInstance", typedEvent);
          isDirty_B_start = true;
          B_start.registerWrappedInstance(typedEvent);
          auditInvocation(B_stop, "B_stop", "registerWrappedInstance", typedEvent);
          isDirty_B_stop = true;
          B_stop.registerWrappedInstance(typedEvent);
          afterEvent();
          return;
      }
    } else if (event instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RemoveService) {
      RemoveService typedEvent = (RemoveService) event;
      auditEvent(typedEvent);
      auditInvocation(A_stop, "A_stop", "removeDependent", typedEvent);
      isDirty_A_stop = true;
      A_stop.removeDependent(typedEvent);
      auditInvocation(B_start, "B_start", "removeDependent", typedEvent);
      isDirty_B_start = true;
      B_start.removeDependent(typedEvent);
      auditInvocation(A_start, "A_start", "removeDependent", typedEvent);
      isDirty_A_start = true;
      A_start.removeDependent(typedEvent);
      auditInvocation(B_stop, "B_stop", "removeDependent", typedEvent);
      isDirty_B_stop = true;
      B_stop.removeDependent(typedEvent);
      auditInvocation(serviceStatusCache, "serviceStatusCache", "removeDependent", typedEvent);
      serviceStatusCache.removeDependent(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart) {
      RequestServiceStart typedEvent = (RequestServiceStart) event;
      auditEvent(typedEvent);
      switch (typedEvent.filterString()) {
          //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[A]
        case ("A"):
          auditInvocation(A_start, "A_start", "startThisService", typedEvent);
          isDirty_A_start = true;
          A_start.startThisService(typedEvent);
          afterEvent();
          return;
          //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStart] filterString:[B]
        case ("B"):
          auditInvocation(B_start, "B_start", "startThisService", typedEvent);
          isDirty_B_start = true;
          B_start.startThisService(typedEvent);
          afterEvent();
          return;
      }
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop) {
      RequestServiceStop typedEvent = (RequestServiceStop) event;
      auditEvent(typedEvent);
      switch (typedEvent.filterString()) {
          //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[A]
        case ("A"):
          auditInvocation(A_stop, "A_stop", "stopThisService", typedEvent);
          isDirty_A_stop = true;
          A_stop.stopThisService(typedEvent);
          afterEvent();
          return;
          //Event Class:[com.fluxtion.example.servicestater.graph.GraphEvent.RequestServiceStop] filterString:[B]
        case ("B"):
          auditInvocation(B_stop, "B_stop", "stopThisService", typedEvent);
          isDirty_B_stop = true;
          B_stop.stopThisService(typedEvent);
          afterEvent();
          return;
      }
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RequestStartAll) {
      RequestStartAll typedEvent = (RequestStartAll) event;
      auditEvent(typedEvent);
      auditInvocation(B_start, "B_start", "startAllServices", typedEvent);
      isDirty_B_start = true;
      B_start.startAllServices(typedEvent);
      auditInvocation(A_start, "A_start", "startAllServices", typedEvent);
      isDirty_A_start = true;
      A_start.startAllServices(typedEvent);
    } else if (event
        instanceof com.fluxtion.example.servicestater.graph.GraphEvent.RequestStopAll) {
      RequestStopAll typedEvent = (RequestStopAll) event;
      auditEvent(typedEvent);
      auditInvocation(A_stop, "A_stop", "stopAllServices", typedEvent);
      isDirty_A_stop = true;
      A_stop.stopAllServices(typedEvent);
      auditInvocation(B_stop, "B_stop", "stopAllServices", typedEvent);
      isDirty_B_stop = true;
      B_stop.stopAllServices(typedEvent);
    } else if (event instanceof com.fluxtion.runtime.audit.EventLogControlEvent) {
      EventLogControlEvent typedEvent = (EventLogControlEvent) event;
      auditEvent(typedEvent);
      auditInvocation(eventLogger, "eventLogger", "calculationLogConfig", typedEvent);
      eventLogger.calculationLogConfig(typedEvent);
    } else if (event instanceof com.fluxtion.runtime.time.ClockStrategy.ClockStrategyEvent) {
      ClockStrategyEvent typedEvent = (ClockStrategyEvent) event;
      auditEvent(typedEvent);
      auditInvocation(clock, "clock", "setClockStrategy", typedEvent);
      isDirty_clock = true;
      clock.setClockStrategy(typedEvent);
    }
  }

  public void triggerCalculation() {
    buffering = false;
    String typedEvent = "No event information - buffered dispatch";
    if (guardCheck_A_start()) {
      auditInvocation(A_start, "A_start", "recalculateStatusForStart", typedEvent);
      isDirty_A_start = true;
      A_start.recalculateStatusForStart();
    }
    if (guardCheck_B_stop()) {
      auditInvocation(B_stop, "B_stop", "recalculateStatusForStop", typedEvent);
      isDirty_B_stop = true;
      B_stop.recalculateStatusForStop();
    }
    if (guardCheck_serviceStatusCache()) {
      auditInvocation(serviceStatusCache, "serviceStatusCache", "publishStatus", typedEvent);
      serviceStatusCache.publishStatus();
    }
    afterEvent();
  }

  private void auditEvent(Object typedEvent) {
    clock.eventReceived(typedEvent);
    eventLogger.eventReceived(typedEvent);
    nodeNameLookup.eventReceived(typedEvent);
  }

  private void auditEvent(Event typedEvent) {
    clock.eventReceived(typedEvent);
    eventLogger.eventReceived(typedEvent);
    nodeNameLookup.eventReceived(typedEvent);
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
    auditor.nodeRegistered(callbackDispatcher, "callbackDispatcher");
    auditor.nodeRegistered(subscriptionManager, "subscriptionManager");
    auditor.nodeRegistered(context, "context");
  }

  private void beforeServiceCall(String functionDescription) {
    functionAudit.setFunctionDescription(functionDescription);
    auditEvent(functionAudit);
    if (buffering) {
      triggerCalculation();
    }
    processing = true;
  }

  private void afterServiceCall() {
    afterEvent();
    callbackDispatcher.dispatchQueuedCallbacks();
    processing = false;
  }

  private void afterEvent() {
    commandPublisher.publishCommands();

    clock.processingComplete();
    eventLogger.processingComplete();
    nodeNameLookup.processingComplete();
    isDirty_A_start = false;
    isDirty_A_stop = false;
    isDirty_B_start = false;
    isDirty_B_stop = false;
    isDirty_clock = false;
  }

  @Override
  public void batchPause() {
    auditEvent(Lifecycle.LifecycleEvent.BatchPause);
    processing = true;

    afterEvent();
    callbackDispatcher.dispatchQueuedCallbacks();
    processing = false;
  }

  @Override
  public void batchEnd() {
    auditEvent(Lifecycle.LifecycleEvent.BatchEnd);
    processing = true;

    afterEvent();
    callbackDispatcher.dispatchQueuedCallbacks();
    processing = false;
  }

  @Override
  public boolean isDirty(Object node) {
    return dirtySupplier(node).getAsBoolean();
  }

  @Override
  public BooleanSupplier dirtySupplier(Object node) {
    if (dirtyFlagSupplierMap.isEmpty()) {
      dirtyFlagSupplierMap.put(A_start, () -> isDirty_A_start);
      dirtyFlagSupplierMap.put(A_stop, () -> isDirty_A_stop);
      dirtyFlagSupplierMap.put(B_start, () -> isDirty_B_start);
      dirtyFlagSupplierMap.put(B_stop, () -> isDirty_B_stop);
      dirtyFlagSupplierMap.put(clock, () -> isDirty_clock);
    }
    return dirtyFlagSupplierMap.getOrDefault(node, StaticEventProcessor.ALWAYS_FALSE);
  }

  @Override
  public void setDirty(Object node, boolean dirtyFlag) {
    if (dirtyFlagUpdateMap.isEmpty()) {
      dirtyFlagUpdateMap.put(A_start, (b) -> isDirty_A_start = b);
      dirtyFlagUpdateMap.put(A_stop, (b) -> isDirty_A_stop = b);
      dirtyFlagUpdateMap.put(B_start, (b) -> isDirty_B_start = b);
      dirtyFlagUpdateMap.put(B_stop, (b) -> isDirty_B_stop = b);
      dirtyFlagUpdateMap.put(clock, (b) -> isDirty_clock = b);
    }
    dirtyFlagUpdateMap.get(node).accept(dirtyFlag);
  }

  private boolean guardCheck_A_start() {
    return isDirty_B_start;
  }

  private boolean guardCheck_B_stop() {
    return isDirty_A_stop;
  }

  private boolean guardCheck_serviceStatusCache() {
    return isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop;
  }

  private boolean guardCheck_commandPublisher() {
    return isDirty_A_start | isDirty_A_stop | isDirty_B_start | isDirty_B_stop;
  }

  private boolean guardCheck_eventLogger() {
    return isDirty_clock;
  }

  @Override
  public <T> T getNodeById(String id) throws NoSuchFieldException {
    return nodeNameLookup.getInstanceById(id);
  }

  @Override
  public <A extends Auditor> A getAuditorById(String id)
      throws NoSuchFieldException, IllegalAccessException {
    return (A) this.getClass().getField(id).get(this);
  }

  @Override
  public void addEventFeed(EventFeed eventProcessorFeed) {
    subscriptionManager.addEventProcessorFeed(eventProcessorFeed);
  }

  @Override
  public void removeEventFeed(EventFeed eventProcessorFeed) {
    subscriptionManager.removeEventProcessorFeed(eventProcessorFeed);
  }

  @Override
  public ProcessorTestLoad newInstance() {
    return new ProcessorTestLoad();
  }

  @Override
  public ProcessorTestLoad newInstance(Map<Object, Object> contextMap) {
    return new ProcessorTestLoad();
  }

  @Override
  public String getLastAuditLogRecord() {
    try {
      EventLogManager eventLogManager =
          (EventLogManager) this.getClass().getField(EventLogManager.NODE_NAME).get(this);
      return eventLogManager.lastRecordAsString();
    } catch (Throwable e) {
      return "";
    }
  }

  public void unKnownEventHandler(Object object) {
    unKnownEventHandler.accept(object);
  }

  @Override
  public <T> void setUnKnownEventHandler(Consumer<T> consumer) {
    unKnownEventHandler = consumer;
  }
}
