package com.example.incident.factory;

import com.example.incident.alert.Alert;
import com.example.incident.channel.NotificationChannel;
import com.example.incident.model.Severity;
import com.example.incident.observer.AlertListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Factory Method: subclasses decide which Alert type to build from the details
 * map; this base class gives every alert its type's channels and the shared listeners.
 */
public abstract class AlertFactory {

    private final List<NotificationChannel> channels;
    private final List<AlertListener> listeners;

    protected AlertFactory(List<NotificationChannel> channels, List<AlertListener> listeners) {
        this.channels = new ArrayList<>(channels);
        this.listeners = new ArrayList<>(listeners);
    }

    public final Alert create(String id, String title, Severity severity, Map<String, String> details) {
        Alert alert = createAlert(id, title, severity, channels, details);
        for (AlertListener listener : listeners) {
            alert.addListener(listener);
        }
        return alert;
    }

    /** The factory method. */
    protected abstract Alert createAlert(String id, String title, Severity severity,
                                         List<NotificationChannel> channels, Map<String, String> details);

    protected static String require(Map<String, String> details, String key) {
        String value = details.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing detail: " + key);
        }
        return value;
    }
}
