package com.example.incident.channel;

import com.example.incident.model.ChannelType;

public class EmailChannelFactory implements ChannelFactory {

    @Override
    public NotificationSender createSender() {
        return new SimulatedSender(ChannelType.EMAIL, 0);
    }

    @Override
    public MessageFormatter createFormatter() {
        return new EmailFormatter();
    }

    @Override
    public DeliveryStatusChecker createStatusChecker() {
        return new ReceiptStatusChecker(ChannelType.EMAIL);
    }
}
