package com.example.incident.channel;

import com.example.incident.model.ChannelType;

public interface NotificationSender {

    ChannelType channel();

    /** Sends the message and returns a receipt id. Throws DeliveryException on failure. */
    String send(String address, String message);
}
