package com.example.incident.channel;

import com.example.incident.model.ChannelType;

public class PushChannelFactory implements ChannelFactory {

    @Override
    public NotificationSender createSender() {
        return new SimulatedSender(ChannelType.PUSH, 0);
    }

    @Override
    public MessageFormatter createFormatter() {
        return new PushFormatter();
    }

    @Override
    public DeliveryStatusChecker createStatusChecker() {
        return new ReceiptStatusChecker(ChannelType.PUSH);
    }
}
