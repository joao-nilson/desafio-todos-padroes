package com.example.incident.channel;

import com.example.incident.model.AlertContent;
import com.example.incident.model.ChannelType;
import com.example.incident.model.DeliveryResult;
import com.example.incident.model.DeliveryStatus;
import com.example.incident.model.Recipient;

/**
 * Assembles a sender, formatter and checker into one channel. The constructor
 * rejects components from different channels, so a mismatched set fails fast.
 */
public class StandardChannel implements NotificationChannel {

    private final NotificationSender sender;
    private final MessageFormatter formatter;
    private final DeliveryStatusChecker checker;

    public StandardChannel(NotificationSender sender, MessageFormatter formatter, DeliveryStatusChecker checker) {
        if (sender.channel() != formatter.channel() || sender.channel() != checker.channel()) {
            throw new IllegalArgumentException("Channel components do not match: "
                    + sender.channel() + "/" + formatter.channel() + "/" + checker.channel());
        }
        this.sender = sender;
        this.formatter = formatter;
        this.checker = checker;
    }

    public static StandardChannel from(ChannelFactory factory) {
        return new StandardChannel(factory.createSender(), factory.createFormatter(), factory.createStatusChecker());
    }

    @Override
    public ChannelType type() {
        return sender.channel();
    }

    @Override
    public DeliveryResult deliver(AlertContent content, Recipient recipient) {
        String address = recipient.addressFor(type());
        if (address == null || address.trim().isEmpty()) {
            return DeliveryResult.skipped(content.getId(), type(), recipient.getName());
        }
        try {
            String receipt = sender.send(address, formatter.format(content));
            DeliveryStatus status = checker.check(receipt);
            return new DeliveryResult(content.getId(), type(), recipient.getName(), status, receipt);
        } catch (DeliveryException e) {
            return DeliveryResult.failed(content.getId(), type(), recipient.getName(), e.getMessage());
        }
    }
}
