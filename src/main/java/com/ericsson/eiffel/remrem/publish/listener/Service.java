package com.ericsson.eiffel.remrem.publish.listener;

/**
 * Interface definition for services.
 * @author esantnc
 * 
 */

public interface Service {
    /**
     * Starts the service. This method blocks until the service has completely started.
     */
    void start();

    /**
     * Stops the service. This method blocks until the service has completely shut down.
     */
    void stop();
}