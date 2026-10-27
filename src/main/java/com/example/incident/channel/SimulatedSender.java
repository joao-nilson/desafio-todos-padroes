package com.example.incident.channel;

import com.example.incident.model.ChannelType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pretends to send. Can be told to fail its first N calls to simulate a flaky provider. */
public class SimulatedSender implements NotificationSender {

    private final ChannelType channel;
    private int failuresLeft;
    private int sequence;
    private final List<String> sent = new ArrayList<>();

    public SimulatedSender(ChannelType channel, int failuresBeforeSuccess) {
        this.channel = channel;
        this.failuresLeft = failuresBeforeSuccess;
    }

    @Override
    public ChannelType channel() {
        return channel;
    }

    @Override
    public String send(String address, String message) {
        if (failuresLeft > 0) {
            failuresLeft--;
            throw new DeliveryException(channel + " provider unavailable");
        }
        sent.add(address + " <- " + message);
        return channel.name().toLowerCase() + "-" + (++sequence);
    }

    public List<String> getSent() {
        return Collections.unmodifiableList(sent);
    }
}
