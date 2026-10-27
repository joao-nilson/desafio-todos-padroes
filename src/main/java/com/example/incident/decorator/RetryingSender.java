package com.example.incident.decorator;

import com.example.incident.channel.DeliveryException;
import com.example.incident.channel.NotificationSender;

/** Retries failed sends immediately (no sleeping, so it stays deterministic). */
public class RetryingSender extends SenderDecorator {

    private final int maxAttempts;

    public RetryingSender(NotificationSender delegate, int maxAttempts) {
        super(delegate);
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        this.maxAttempts = maxAttempts;
    }

    @Override
    public String send(String address, String message) {
        DeliveryException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return super.send(address, message);
            } catch (DeliveryException e) {
                last = e;
            }
        }
        throw new DeliveryException("Failed after " + maxAttempts + " attempts: " + last.getMessage());
    }
}
