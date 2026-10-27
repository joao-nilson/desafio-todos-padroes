package com.example.incident.factory;

import com.example.incident.TestSupport.RecordingChannel;
import com.example.incident.TestSupport.RecordingListener;
import com.example.incident.alert.Alert;
import com.example.incident.alert.ServiceOutageAlert;
import com.example.incident.alert.ThresholdAlert;
import com.example.incident.model.ChannelType;
import com.example.incident.model.Severity;
import com.example.incident.observer.AlertListener;
import com.example.incident.state.AlertStatus;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.incident.TestSupport.channels;
import static org.junit.jupiter.api.Assertions.*;

class AlertFactoryTest {

    private final RecordingChannel email = new RecordingChannel(ChannelType.EMAIL);
    private final List<AlertListener> noListeners = Collections.emptyList();

    private static Map<String, String> map(String... keyValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    @Test
    void thresholdFactoryCreatesThresholdAlert() {
        AlertFactory factory = new ThresholdAlertFactory(channels(email), noListeners);

        Alert alert = factory.create("A-1", "High CPU", Severity.HIGH,
                map("metric", "cpu_usage", "value", "97", "threshold", "90"));

        assertTrue(alert instanceof ThresholdAlert);
        assertEquals("THRESHOLD", alert.getType());
        assertEquals("cpu_usage", alert.getDedupKey());
        assertEquals(AlertStatus.NEW, alert.getStatus());
        assertTrue(alert.getHeadline().contains("cpu_usage"));
        assertEquals(1, alert.getChannels().size());
    }

    @Test
    void outageFactoryCreatesServiceOutageAlert() {
        AlertFactory factory = new ServiceOutageAlertFactory(channels(email), noListeners);

        Alert alert = factory.create("A-2", "Outage", Severity.CRITICAL,
                map("service", "checkout", "region", "sa-east-1"));

        assertTrue(alert instanceof ServiceOutageAlert);
        assertEquals("OUTAGE", alert.getType());
        assertEquals("checkout@sa-east-1", alert.getDedupKey());
        assertTrue(alert.getHeadline().contains("checkout"));
    }

    @Test
    void missingDetailIsRejected() {
        AlertFactory factory = new ThresholdAlertFactory(channels(email), noListeners);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> factory.create("A-1", "High CPU", Severity.HIGH, map("value", "97", "threshold", "90")));

        assertTrue(e.getMessage().contains("metric"));
    }

    @Test
    void blankTitleIsRejected() {
        AlertFactory factory = new ServiceOutageAlertFactory(channels(email), noListeners);

        assertThrows(IllegalArgumentException.class,
                () -> factory.create("A-2", "  ", Severity.HIGH, map("service", "x", "region", "y")));
    }

    @Test
    void alertWithoutChannelsIsRejected() {
        AlertFactory factory = new ServiceOutageAlertFactory(Collections.<com.example.incident.channel.NotificationChannel>emptyList(), noListeners);

        assertThrows(IllegalArgumentException.class,
                () -> factory.create("A-2", "Outage", Severity.HIGH, map("service", "x", "region", "y")));
    }

    @Test
    void factoryAttachesItsListenersToEveryAlertItCreates() {
        RecordingListener listener = new RecordingListener();
        AlertFactory factory = new ServiceOutageAlertFactory(channels(email), Collections.<AlertListener>singletonList(listener));

        Alert first = factory.create("A-1", "Outage", Severity.HIGH, map("service", "a", "region", "r"));
        Alert second = factory.create("A-2", "Outage", Severity.HIGH, map("service", "b", "region", "r"));
        first.acknowledge();
        second.acknowledge();

        assertEquals(2, listener.events.size());
    }
}
