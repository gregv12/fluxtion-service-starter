package com.fluxtion.example.servicestater;

public enum ServiceStatus {
    STATUS_UNKNOWN,
    WAITING_FOR_PARENTS_TO_START,
    STARTING,
    STARTED,
    WAITING_FOR_PARENTS_TO_STOP,
    STOPPING,
    STOPPED,
}
