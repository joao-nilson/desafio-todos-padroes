package com.example.incident.decorator;

import com.example.incident.channel.DeliveryException;
import com.example.incident.channel.NotificationSender;

import java.util.function.Consumer;

/** Logs every attempt and its outcome, then returns or rethrows exactly what the delegate did. */
public class LoggingSender extends SenderDecorator {

    private final Consumer<String> log;

    public LoggingSender(NotificationSender delegate, Consumer<String> log) {
        super(delegate);
        this.log = log;
    }

    @Override
    public String send(String address, String message) {
        log.accept("SEND " + channel() + " to " + address);
        try {
            String receipt = super.send(address, message);
            log.accept("OK   " + channel() + " receipt=" + receipt);
            return receipt;
        } catch (DeliveryException e) {
            log.accept("FAIL " + channel() + " " + e.getMessage());
            throw e;
        }
    }
}
