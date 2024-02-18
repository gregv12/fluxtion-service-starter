package com.fluxtion.example.servicestater;

import lombok.Value;

/**
 * Immutable data type representing the start/stop order of a service. Optionally gives access to a wrapped instance
 * that can be associated with the {@link Service}
 * @param <T>
 */
@Value
public class ServiceOrderRecord<T> {
    String serviceName;
    T wrappedInstance;
    Service.Status status;
}
