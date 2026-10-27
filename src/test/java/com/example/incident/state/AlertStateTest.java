package com.example.incident.state;

import com.example.incident.TestSupport.RecordingChannel;
import com.example.incident.alert.Alert;
import com.example.incident.model.ChannelType;
import com.example.incident.model.Severity;
import org.junit.jupiter.api.Test;

import static com.example.incident.TestSupport.threshold;
import static org.junit.jupiter.api.Assertions.*;

class AlertStateTest {

    private Alert newAlert() {
        return threshold(Severity.HIGH, new RecordingChannel(ChannelType.EMAIL));
    }

    @Test
    void newAlertStartsInNewState() {
        assertEquals(AlertStatus.NEW, newAlert().getStatus());
    }

    @Test
    void walksThroughNormalLifecycle() {
        Alert alert = newAlert();

        alert.acknowledge();
        assertEquals(AlertStatus.ACKNOWLEDGED, alert.getStatus());

        alert.resolve();
        assertEquals(AlertStatus.RESOLVED, alert.getStatus());
    }

    @Test
    void newAlertCanBeResolvedDirectly() {
        Alert alert = newAlert();

        alert.resolve();

        assertEquals(AlertStatus.RESOLVED, alert.getStatus());
    }

    @Test
    void acknowledgingTwiceIsRejectedAndKeepsTheState() {
        Alert alert = newAlert();
        alert.acknowledge();

        InvalidTransitionException e = assertThrows(InvalidTransitionException.class, alert::acknowledge);

        assertEquals("Cannot acknowledge an alert that is ACKNOWLEDGED", e.getMessage());
        assertEquals(AlertStatus.ACKNOWLEDGED, alert.getStatus());
    }

    @Test
    void resolvedIsTerminal() {
        Alert alert = newAlert();
        alert.resolve();

        assertThrows(InvalidTransitionException.class, alert::acknowledge);
        assertThrows(InvalidTransitionException.class, alert::resolve);
        assertEquals(AlertStatus.RESOLVED, alert.getStatus());
    }

    @Test
    void stateObjectsDefineTheTransitionTable() {
        assertEquals(AlertStatus.ACKNOWLEDGED, new NewState().acknowledge().status());
        assertEquals(AlertStatus.RESOLVED, new NewState().resolve().status());
        assertEquals(AlertStatus.RESOLVED, new AcknowledgedState().resolve().status());
        assertThrows(InvalidTransitionException.class, () -> new AcknowledgedState().acknowledge());
        assertThrows(InvalidTransitionException.class, () -> new ResolvedState().resolve());
    }
}
