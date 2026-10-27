package com.example.incident.factory;

import com.example.incident.alert.Alert;
import com.example.incident.alert.ThresholdAlert;
import com.example.incident.channel.NotificationChannel;
import com.example.incident.model.Severity;
import com.example.incident.observer.AlertListener;

import java.util.List;
import java.util.Map;

/** Expects details: metric, value, threshold. */
public class ThresholdAlertFactory extends AlertFactory {

    public ThresholdAlertFactory(List<NotificationChannel> channels, List<AlertListener> listeners) {
        super(channels, listeners);
    }

    @Override
    protected Alert createAlert(String id, String title, Severity severity,
                                List<NotificationChannel> channels, Map<String, String> details) {
        return new ThresholdAlert(id, title, severity, channels,
                require(details, "metric"),
                Double.parseDouble(require(details, "value")),
                Double.parseDouble(require(details, "threshold")));
    }
}
