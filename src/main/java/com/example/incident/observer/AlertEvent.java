package com.example.incident.observer;

import com.example.incident.model.DeliveryResult;
import com.example.incident.state.AlertStatus;

public final class AlertEvent {

    public enum Type { STATUS_CHANGED, DELIVERY_RECORDED }

    private final String alertId;
    private final Type type;
    private final AlertStatus status;
    private final String detail;
    private final DeliveryResult deliveryResult;

    private AlertEvent(String alertId, Type type, AlertStatus status, String detail, DeliveryResult result) {
        this.alertId = alertId;
        this.type = type;
        this.status = status;
        this.detail = detail;
        this.deliveryResult = result;
    }

    public static AlertEvent statusChanged(String alertId, AlertStatus from, AlertStatus to) {
        return new AlertEvent(alertId, Type.STATUS_CHANGED, to, from + " -> " + to, null);
    }

    public static AlertEvent deliveryRecorded(String alertId, AlertStatus current, DeliveryResult result) {
        return new AlertEvent(alertId, Type.DELIVERY_RECORDED, current, result.toString(), result);
    }

    public String getAlertId() {
        return alertId;
    }

    public Type getType() {
        return type;
    }

    /** The alert's status at the time of the event. */
    public AlertStatus getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    /** Only set for DELIVERY_RECORDED events. */
    public DeliveryResult getDeliveryResult() {
        return deliveryResult;
    }

    @Override
    public String toString() {
        return type + " alert=" + alertId + " " + detail;
    }
}
