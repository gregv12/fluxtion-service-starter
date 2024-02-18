package com.fluxtion.example.servicestater;

import com.fluxtion.runtime.annotations.NoPropagateFunction;

import java.util.function.Consumer;

public interface ServiceQuery {

    /**
     * Invokes the supplied consumer with all the {@link ServiceOrderRecord}'s in the start order for all the services
     * managed by this {@link ServiceManager}
     *
     * @param serviceConsumer client receiver of {@link ServiceOrderRecord}'s
     */
    @NoPropagateFunction
    void startOrder(Consumer<ServiceOrderRecord<?>> serviceConsumer);

    /**
     * Invokes the supplied consumer with all the {@link ServiceOrderRecord}'s in the stop order for all the services
     * managed by this {@link ServiceManager}
     *
     * @param serviceConsumer client receiver of {@link ServiceOrderRecord}'s
     */
    @NoPropagateFunction
    void stopOrder(Consumer<ServiceOrderRecord<?>> serviceConsumer);
}
