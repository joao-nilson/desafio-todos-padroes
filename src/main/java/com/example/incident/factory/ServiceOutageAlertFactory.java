package com.example.incident.factory;

import com.example.incident.alert.Alert;
import com.example.incident.alert.ServiceOutageAlert;
import com.example.incident.channel.NotificationChannel;
import com.example.incident.model.Severity;
import com.example.incident.observer.AlertListener;

import java.util.List;
import java.util.Map;

/** Expects details: service, region. */
public class ServiceOutageAlertFactory extends AlertFactory {

    public ServiceOutageAlertFactory(List<NotificationChannel> channels, List<AlertListener> listeners) {
        super(channels, listeners);
    }

    @Override
    protected Alert createAlert(String id, String title, Severity severity,
                                List<NotificationChannel> channels, Map<String, String> details) {
        return new ServiceOutageAlert(id, title, severity, channels,
                require(details, "service"),
                require(details, "region"));
    }
}
