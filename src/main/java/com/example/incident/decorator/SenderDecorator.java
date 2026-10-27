package com.example.incident.decorator;

import com.example.incident.channel.NotificationSender;
import com.example.incident.model.ChannelType;

/** Base decorator: forwards everything to the wrapped sender. */
public abstract class SenderDecorator implements NotificationSender {

    protected final NotificationSender delegate;

    protected SenderDecorator(NotificationSender delegate) {
        this.delegate = delegate;
    }

    @Override
    public ChannelType channel() {
        return delegate.channel();
    }

    @Override
    public String send(String address, String message) {
        return delegate.send(address, message);
    }
}
