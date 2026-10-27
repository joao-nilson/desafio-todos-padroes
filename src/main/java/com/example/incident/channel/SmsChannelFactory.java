package com.example.incident.channel;

import com.example.incident.model.ChannelType;

public class SmsChannelFactory implements ChannelFactory {

    private final int simulatedFailures;

    public SmsChannelFactory() {
        this(0);
    }

    /** @param simulatedFailures how many sends fail before the simulated provider recovers */
    public SmsChannelFactory(int simulatedFailures) {
        this.simulatedFailures = simulatedFailures;
    }

    @Override
    public NotificationSender createSender() {
        return new SimulatedSender(ChannelType.SMS, simulatedFailures);
    }

    @Override
    public MessageFormatter createFormatter() {
        return new SmsFormatter();
    }

    @Override
    public DeliveryStatusChecker createStatusChecker() {
        return new ReceiptStatusChecker(ChannelType.SMS);
    }
}
