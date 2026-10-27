package com.example.incident.bridge;

import com.example.incident.TestSupport.RecordingChannel;
import com.example.incident.alert.Alert;
import com.example.incident.model.ChannelType;
import com.example.incident.model.DeliveryResult;
import com.example.incident.model.Severity;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.incident.TestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class AlertChannelBridgeTest {

    private final RecordingChannel email = new RecordingChannel(ChannelType.EMAIL);
    private final RecordingChannel sms = new RecordingChannel(ChannelType.SMS);
    private final RecordingChannel push = new RecordingChannel(ChannelType.PUSH);

    /** A brand-new alert type, defined here to show that channels need no changes for it. */
    private static class MaintenanceAlert extends Alert {
        MaintenanceAlert(RecordingChannel... channels) {
            super("M-1", "Maintenance", Severity.LOW, channels(channels));
        }

        @Override
        public String getType() {
            return "MAINTENANCE";
        }

        @Override
        public String getDedupKey() {
            return "window";
        }

        @Override
        public String getHeadline() {
            return "Planned maintenance tonight";
        }

        @Override
        public String getDetails() {
            return "Expect brief downtime.";
        }
    }

    @Test
    void oneAlertTypeCanUseSeveralChannels() {
        Alert alert = threshold(Severity.HIGH, email, sms, push);

        List<DeliveryResult> results = alert.dispatchTo(Collections.singletonList(everywhere("ana")));

        Set<ChannelType> used = new HashSet<>();
        for (DeliveryResult result : results) {
            used.add(result.getChannel());
        }
        assertEquals(EnumSet.of(ChannelType.EMAIL, ChannelType.SMS, ChannelType.PUSH), used);
    }

    @Test
    void oneChannelServesDifferentAlertTypesWithTypeSpecificContent() {
        Alert threshold = threshold(Severity.HIGH, email);
        Alert outage = outage("A-2", Severity.CRITICAL, email);

        threshold.dispatchTo(Collections.singletonList(everywhere("ana")));
        outage.dispatchTo(Collections.singletonList(everywhere("ana")));

        assertEquals(2, email.delivered.size()); // same channel instance received both
        assertTrue(email.delivered.get(0).getHeadline().contains("cpu_usage"));
        assertTrue(email.delivered.get(1).getHeadline().contains("checkout"));
    }

    @Test
    void differentAlertTypesCanBeRoutedToDifferentChannels() {
        Alert threshold = threshold(Severity.HIGH, email);
        Alert outage = outage("A-2", Severity.CRITICAL, sms, push);

        threshold.dispatchTo(Collections.singletonList(everywhere("ana")));
        outage.dispatchTo(Collections.singletonList(everywhere("ana")));

        assertEquals(1, email.delivered.size());
        assertEquals("A-1", email.delivered.get(0).getId());
        assertEquals(1, sms.delivered.size());
        assertEquals("A-2", sms.delivered.get(0).getId());
        assertEquals(1, push.delivered.size());
    }

    @Test
    void everyRecipientIsReachedOnEveryChannel() {
        Alert alert = threshold(Severity.HIGH, email, sms);

        List<DeliveryResult> results = alert.dispatchTo(Arrays.asList(everywhere("ana"), everywhere("bob")));

        assertEquals(4, results.size());
        assertEquals(Arrays.asList("ana", "bob"), email.recipientNames);
        assertEquals(Arrays.asList("ana", "bob"), sms.recipientNames);
    }

    @Test
    void newAlertTypeWorksWithExistingChannelsWithoutNewSubclasses() {
        Alert alert = new MaintenanceAlert(email, push);

        alert.dispatchTo(Collections.singletonList(everywhere("ana")));

        assertEquals("Planned maintenance tonight", email.delivered.get(0).getHeadline());
        assertEquals(1, push.delivered.size());
    }

    @Test
    void channelListIsReadOnlyFromOutside() {
        Alert alert = threshold(Severity.HIGH, email);

        assertThrows(UnsupportedOperationException.class, () -> alert.getChannels().add(sms));
    }
}
