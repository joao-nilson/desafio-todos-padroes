package com.example.incident.observer;

import com.example.incident.model.DeliveryStatus;
import com.example.incident.state.AlertStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/** Keeps a current-state view: latest status per alert plus delivery counters. */
public class DashboardListener implements AlertListener {

    private final Map<String, AlertStatus> statusByAlert = new LinkedHashMap<>();
    private int delivered;
    private int failed;
    private int skipped;

    @Override
    public void onEvent(AlertEvent event) {
        statusByAlert.put(event.getAlertId(), event.getStatus());
        if (event.getType() == AlertEvent.Type.DELIVERY_RECORDED) {
            DeliveryStatus status = event.getDeliveryResult().getStatus();
            if (status == DeliveryStatus.DELIVERED) {
                delivered++;
            } else if (status == DeliveryStatus.FAILED) {
                failed++;
            } else if (status == DeliveryStatus.SKIPPED) {
                skipped++;
            }
        }
    }

    public AlertStatus getStatus(String alertId) {
        return statusByAlert.get(alertId);
    }

    public int getDeliveredCount() {
        return delivered;
    }

    public int getFailedCount() {
        return failed;
    }

    public int getSkippedCount() {
        return skipped;
    }

    public String summary() {
        return "alerts=" + statusByAlert + " delivered=" + delivered
                + " failed=" + failed + " skipped=" + skipped;
    }
}
