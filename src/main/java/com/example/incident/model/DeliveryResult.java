package com.example.incident.model;

public class DeliveryResult {

    private final String alertId;
    private final ChannelType channel;
    private final String recipientName;
    private final DeliveryStatus status;
    private final String detail;

    public DeliveryResult(String alertId, ChannelType channel, String recipientName,
                          DeliveryStatus status, String detail) {
        this.alertId = alertId;
        this.channel = channel;
        this.recipientName = recipientName;
        this.status = status;
        this.detail = detail;
    }

    public static DeliveryResult skipped(String alertId, ChannelType channel, String recipientName) {
        return new DeliveryResult(alertId, channel, recipientName, DeliveryStatus.SKIPPED, "no address");
    }

    public static DeliveryResult failed(String alertId, ChannelType channel, String recipientName, String reason) {
        return new DeliveryResult(alertId, channel, recipientName, DeliveryStatus.FAILED, reason);
    }

    public String getAlertId() {
        return alertId;
    }

    public ChannelType getChannel() {
        return channel;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return channel + " -> " + recipientName + ": " + status + " (" + detail + ")";
    }
}
