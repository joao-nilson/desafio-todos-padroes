package com.example.incident.channel;

import com.example.incident.model.ChannelType;
import com.example.incident.model.DeliveryStatus;

/** Simulated checker: only recognizes receipts issued by its own channel. */
public class ReceiptStatusChecker implements DeliveryStatusChecker {

    private final ChannelType channel;

    public ReceiptStatusChecker(ChannelType channel) {
        this.channel = channel;
    }

    @Override
    public ChannelType channel() {
        return channel;
    }

    @Override
    public DeliveryStatus check(String receiptId) {
        String prefix = channel.name().toLowerCase() + "-";
        return receiptId != null && receiptId.startsWith(prefix)
                ? DeliveryStatus.DELIVERED
                : DeliveryStatus.UNKNOWN;
    }
}
