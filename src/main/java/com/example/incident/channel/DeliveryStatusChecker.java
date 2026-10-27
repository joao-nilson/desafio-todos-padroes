package com.example.incident.channel;

import com.example.incident.model.ChannelType;
import com.example.incident.model.DeliveryStatus;

public interface DeliveryStatusChecker {

    ChannelType channel();

    DeliveryStatus check(String receiptId);
}
