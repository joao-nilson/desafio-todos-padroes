package com.example.incident;

import com.example.incident.alert.Alert;
import com.example.incident.alert.ServiceOutageAlert;
import com.example.incident.alert.ThresholdAlert;
import com.example.incident.channel.NotificationChannel;
import com.example.incident.model.AlertContent;
import com.example.incident.model.ChannelType;
import com.example.incident.model.DeliveryResult;
import com.example.incident.model.DeliveryStatus;
import com.example.incident.model.Recipient;
import com.example.incident.model.Severity;
import com.example.incident.observer.AlertEvent;
import com.example.incident.observer.AlertListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Small fakes and builders shared by the tests. */
public final class TestSupport {

    private TestSupport() {
    }

    public static List<NotificationChannel> channels(NotificationChannel... channels) {
        return Arrays.asList(channels);
    }

    public static Alert threshold(Severity severity, NotificationChannel... channels) {
        return threshold("A-1", "cpu_usage", severity, channels);
    }

    public static Alert threshold(String id, String metric, Severity severity, NotificationChannel... channels) {
        return new ThresholdAlert(id, "High CPU", severity, channels(channels), metric, 97, 90);
    }

    public static Alert outage(String id, Severity severity, NotificationChannel... channels) {
        return new ServiceOutageAlert(id, "Outage", severity, channels(channels), "checkout", "sa-east-1");
    }

    /** A recipient reachable on every channel who accepts every severity. */
    public static Recipient everywhere(String name) {
        return new Recipient(name, name + "@example.com", "+1-555-" + name, name + "-device", Severity.LOW);
    }

    /** Channel fake: records what it was asked to deliver and always reports DELIVERED. */
    public static class RecordingChannel implements NotificationChannel {
        private final ChannelType type;
        public final List<AlertContent> delivered = new ArrayList<>();
        public final List<String> recipientNames = new ArrayList<>();

        public RecordingChannel(ChannelType type) {
            this.type = type;
        }

        @Override
        public ChannelType type() {
            return type;
        }

        @Override
        public DeliveryResult deliver(AlertContent content, Recipient recipient) {
            delivered.add(content);
            recipientNames.add(recipient.getName());
            return new DeliveryResult(content.getId(), type, recipient.getName(), DeliveryStatus.DELIVERED, "recorded");
        }
    }

    /** Listener fake: keeps every event it receives. */
    public static class RecordingListener implements AlertListener {
        public final List<AlertEvent> events = new ArrayList<>();

        @Override
        public void onEvent(AlertEvent event) {
            events.add(event);
        }
    }
}
