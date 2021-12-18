package com.fluxtion.example.servicestater;

import lombok.Value;

/**
 * Immutable datatype representing the status of a service.
 */
@Value
public class StatusForService {
    String serviceName;
    Service.Status status;
}
