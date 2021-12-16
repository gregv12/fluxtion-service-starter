package com.fluxtion.example.servicestater;

import com.fluxtion.runtim.Named;

import java.util.Collections;
import java.util.List;

/**
 * A representation of an external service and its dependencies
 */
public class Service implements Named {

    private final String name;
    private final List<Service> dependencies;

    public Service(String name, List<Service> dependencies) {
        this.name = name;
        this.dependencies = dependencies;
    }

    public Service(String serviceName, Service... services) {
        this(serviceName, services==null? Collections.emptyList():List.of(services));
    }

    public List<Service> getDependencies() {
        return dependencies;
    }

    @Override
    public String getName() {
        return name;
    }
}
