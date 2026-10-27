package com.example.incident.channel;

public class DeliveryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DeliveryException(String message) {
        super(message);
    }
}
