package com.shopzone.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an inter-service REST call fails.
 * This helps distinguish between "our logic failed" vs "another service is down."
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceCommunicationException extends RuntimeException {
    private final String serviceName;

    public ServiceCommunicationException(String serviceName, String message) {
        super("Failed to communicate with " + serviceName + ": " + message);
        this.serviceName = serviceName;
    }

    public ServiceCommunicationException(String serviceName, String message, Throwable cause) {
        super("Failed to communicate with " + serviceName + ": " + message, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
