package com.example.incident.channel;

import com.example.incident.model.AlertContent;
import com.example.incident.model.ChannelType;
import com.example.incident.model.DeliveryResult;
import com.example.incident.model.Recipient;

/** Bridge implementor: how an alert's content reaches a recipient. */
public interface NotificationChannel {

    ChannelType type();

    DeliveryResult deliver(AlertContent content, Recipient recipient);
}
