package com.example.incident.alert;

import com.example.incident.channel.NotificationChannel;
import com.example.incident.model.AlertContent;
import com.example.incident.model.DeliveryResult;
import com.example.incident.model.Recipient;
import com.example.incident.model.Severity;
import com.example.incident.observer.AlertEvent;
import com.example.incident.observer.AlertListener;
import com.example.incident.state.AlertState;
import com.example.incident.state.AlertStatus;
import com.example.incident.state.NewState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One class playing three roles, each kept small:
 * - Bridge abstraction: subclasses provide content, {@link NotificationChannel}s deliver it.
 * - State context: delegates lifecycle transitions to the current {@link AlertState}.
 * - Observer subject: tells listeners about status changes and delivery results.
 */
public abstract class Alert implements AlertContent {

    private final String id;
    private final String title;
    private final Severity severity;
    private final List<NotificationChannel> channels;
    private final List<AlertListener> listeners = new ArrayList<>();
    private final List<DeliveryResult> deliveryResults = new ArrayList<>();
    private final Map<String, String> metadata = new LinkedHashMap<>();
    private AlertState state = new NewState();

    protected Alert(String id, String title, Severity severity, List<NotificationChannel> channels) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Alert id is required");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Alert title is required");
        }
        if (severity == null) {
            throw new IllegalArgumentException("Alert severity is required");
        }
        if (channels == null || channels.isEmpty()) {
            throw new IllegalArgumentException("Alert needs at least one channel");
        }
        this.id = id;
        this.title = title;
        this.severity = severity;
        this.channels = new ArrayList<>(channels);
    }

    // ----- content (varies by alert type) -----

    /** Short name of the alert type, e.g. "THRESHOLD". */
    public abstract String getType();

    /** Alerts with the same dedup key are considered the same incident. */
    public abstract String getDedupKey();

    @Override
    public abstract String getHeadline();

    @Override
    public abstract String getDetails();

    @Override
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public Severity getSeverity() {
        return severity;
    }

    // ----- bridge: delivery goes through whatever channels this alert was given -----

    public List<NotificationChannel> getChannels() {
        return Collections.unmodifiableList(channels);
    }

    /** Sends this alert through every channel to every recipient. Does not record the results. */
    public List<DeliveryResult> dispatchTo(List<Recipient> recipients) {
        List<DeliveryResult> results = new ArrayList<>();
        for (NotificationChannel channel : channels) {
            for (Recipient recipient : recipients) {
                results.add(channel.deliver(this, recipient));
            }
        }
        return results;
    }

    // ----- state -----

    public AlertStatus getStatus() {
        return state.status();
    }

    public void acknowledge() {
        transitionTo(state.acknowledge());
    }

    public void resolve() {
        transitionTo(state.resolve());
    }

    private void transitionTo(AlertState next) {
        AlertStatus from = state.status();
        state = next;
        publish(AlertEvent.statusChanged(id, from, next.status()));
    }

    // ----- delivery results & metadata -----

    public void recordDelivery(DeliveryResult result) {
        deliveryResults.add(result);
        publish(AlertEvent.deliveryRecorded(id, getStatus(), result));
    }

    public List<DeliveryResult> getDeliveryResults() {
        return Collections.unmodifiableList(deliveryResults);
    }

    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    // ----- observer -----

    public void addListener(AlertListener listener) {
        listeners.add(listener);
    }

    private void publish(AlertEvent event) {
        for (AlertListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
