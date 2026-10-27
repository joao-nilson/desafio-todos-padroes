package com.example.incident.observer;

import com.example.incident.TestSupport.RecordingChannel;
import com.example.incident.TestSupport.RecordingListener;
import com.example.incident.alert.Alert;
import com.example.incident.model.ChannelType;
import com.example.incident.model.DeliveryResult;
import com.example.incident.model.DeliveryStatus;
import com.example.incident.model.Severity;
import com.example.incident.state.AlertStatus;
import com.example.incident.state.InvalidTransitionException;
import org.junit.jupiter.api.Test;

import static com.example.incident.TestSupport.threshold;
import static org.junit.jupiter.api.Assertions.*;

class ObserverTest {

    private final Alert alert = threshold(Severity.HIGH, new RecordingChannel(ChannelType.EMAIL));

    private static DeliveryResult result(DeliveryStatus status) {
        return new DeliveryResult("A-1", ChannelType.EMAIL, "ana", status, "detail");
    }

    @Test
    void listenerReceivesStatusChangeEvent() {
        RecordingListener listener = new RecordingListener();
        alert.addListener(listener);

        alert.acknowledge();

        assertEquals(1, listener.events.size());
        AlertEvent event = listener.events.get(0);
        assertEquals(AlertEvent.Type.STATUS_CHANGED, event.getType());
        assertEquals("A-1", event.getAlertId());
        assertEquals(AlertStatus.ACKNOWLEDGED, event.getStatus());
        assertEquals("NEW -> ACKNOWLEDGED", event.getDetail());
    }

    @Test
    void listenerReceivesDeliveryResultEvent() {
        RecordingListener listener = new RecordingListener();
        alert.addListener(listener);
        DeliveryResult delivered = result(DeliveryStatus.DELIVERED);

        alert.recordDelivery(delivered);

        assertEquals(1, listener.events.size());
        assertEquals(AlertEvent.Type.DELIVERY_RECORDED, listener.events.get(0).getType());
        assertSame(delivered, listener.events.get(0).getDeliveryResult());
        assertEquals(1, alert.getDeliveryResults().size());
    }

    @Test
    void allListenersAreNotified() {
        RecordingListener first = new RecordingListener();
        RecordingListener second = new RecordingListener();
        alert.addListener(first);
        alert.addListener(second);

        alert.resolve();

        assertEquals(1, first.events.size());
        assertEquals(1, second.events.size());
    }

    @Test
    void rejectedTransitionPublishesNothing() {
        RecordingListener listener = new RecordingListener();
        alert.resolve();
        alert.addListener(listener);

        assertThrows(InvalidTransitionException.class, alert::acknowledge);

        assertTrue(listener.events.isEmpty());
    }

    @Test
    void listenerAddedLaterOnlySeesLaterEvents() {
        alert.acknowledge();
        RecordingListener late = new RecordingListener();
        alert.addListener(late);

        alert.resolve();

        assertEquals(1, late.events.size());
        assertEquals("ACKNOWLEDGED -> RESOLVED", late.events.get(0).getDetail());
    }

    @Test
    void auditLogKeepsEveryEventInOrder() {
        AuditLogListener audit = new AuditLogListener();
        alert.addListener(audit);

        alert.recordDelivery(result(DeliveryStatus.DELIVERED));
        alert.acknowledge();

        assertEquals(2, audit.getEntries().size());
        assertTrue(audit.getEntries().get(0).startsWith("DELIVERY_RECORDED alert=A-1"));
        assertEquals("STATUS_CHANGED alert=A-1 NEW -> ACKNOWLEDGED", audit.getEntries().get(1));
        assertThrows(UnsupportedOperationException.class, () -> audit.getEntries().add("tamper"));
    }

    @Test
    void dashboardTracksLatestStatusAndDeliveryCounts() {
        DashboardListener dashboard = new DashboardListener();
        alert.addListener(dashboard);

        alert.recordDelivery(result(DeliveryStatus.DELIVERED));
        alert.recordDelivery(result(DeliveryStatus.DELIVERED));
        alert.recordDelivery(result(DeliveryStatus.FAILED));
        alert.recordDelivery(result(DeliveryStatus.SKIPPED));
        assertEquals(AlertStatus.NEW, dashboard.getStatus("A-1"));

        alert.acknowledge();
        alert.resolve();

        assertEquals(AlertStatus.RESOLVED, dashboard.getStatus("A-1"));
        assertEquals(2, dashboard.getDeliveredCount());
        assertEquals(1, dashboard.getFailedCount());
        assertEquals(1, dashboard.getSkippedCount());
        assertNull(dashboard.getStatus("unknown"));
    }
}
