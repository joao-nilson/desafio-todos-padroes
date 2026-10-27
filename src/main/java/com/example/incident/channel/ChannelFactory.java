package com.example.incident.channel;

/**
 * Abstract Factory: each implementation returns a matching sender, formatter
 * and status checker for one channel.
 */
public interface ChannelFactory {

    NotificationSender createSender();

    MessageFormatter createFormatter();

    DeliveryStatusChecker createStatusChecker();
}
