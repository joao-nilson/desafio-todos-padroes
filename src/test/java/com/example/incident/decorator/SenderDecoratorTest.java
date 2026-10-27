package com.example.incident.decorator;

import com.example.incident.channel.DeliveryException;
import com.example.incident.channel.NotificationSender;
import com.example.incident.channel.SimulatedSender;
import com.example.incident.model.ChannelType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SenderDecoratorTest {

    /** Fake sender: fails a set number of times, then succeeds. Counts calls. */
    private static class FakeSender implements NotificationSender {
        int calls;
        private final int failures;

        FakeSender(int failures) {
            this.failures = failures;
        }

        @Override
        public ChannelType channel() {
            return ChannelType.SMS;
        }

        @Override
        public String send(String address, String message) {
            calls++;
            if (calls <= failures) {
                throw new DeliveryException("boom " + calls);
            }
            return "sms-" + calls;
        }
    }

    // ----- retrying -----

    @Test
    void retryingSenderRecoversFromTransientFailures() {
        FakeSender fake = new FakeSender(2);

        String receipt = new RetryingSender(fake, 3).send("+1", "hi");

        assertEquals("sms-3", receipt);
        assertEquals(3, fake.calls);
    }

    @Test
    void retryingSenderGivesUpAfterMaxAttempts() {
        FakeSender fake = new FakeSender(10);

        DeliveryException e = assertThrows(DeliveryException.class,
                () -> new RetryingSender(fake, 3).send("+1", "hi"));

        assertEquals(3, fake.calls);
        assertTrue(e.getMessage().contains("3 attempts"));
        assertTrue(e.getMessage().contains("boom 3"));
    }

    @Test
    void retryingSenderDoesNotRetryAfterSuccess() {
        FakeSender fake = new FakeSender(0);

        new RetryingSender(fake, 5).send("+1", "hi");

        assertEquals(1, fake.calls);
    }

    @Test
    void retryingSenderRequiresAtLeastOneAttempt() {
        assertThrows(IllegalArgumentException.class, () -> new RetryingSender(new FakeSender(0), 0));
    }

    // ----- logging -----

    @Test
    void loggingSenderLogsAndReturnsTheDelegatesReceiptUnchanged() {
        List<String> log = new ArrayList<>();
        FakeSender fake = new FakeSender(0);

        String receipt = new LoggingSender(fake, log::add).send("+1", "hi");

        assertEquals("sms-1", receipt);
        assertEquals(2, log.size());
        assertTrue(log.get(0).contains("SEND SMS to +1"));
        assertTrue(log.get(1).contains("OK") && log.get(1).contains("sms-1"));
    }

    @Test
    void loggingSenderLogsFailureAndRethrowsTheSameException() {
        List<String> log = new ArrayList<>();
        LoggingSender sender = new LoggingSender(new FakeSender(1), log::add);

        DeliveryException e = assertThrows(DeliveryException.class, () -> sender.send("+1", "hi"));

        assertEquals("boom 1", e.getMessage());
        assertTrue(log.get(1).startsWith("FAIL"));
    }

    // ----- both -----

    @Test
    void decoratorsKeepTheWrappedSendersChannelAndCoreBehavior() {
        SimulatedSender core = new SimulatedSender(ChannelType.PUSH, 0);
        NotificationSender decorated = new RetryingSender(new LoggingSender(core, line -> { }), 2);

        String receipt = decorated.send("device-1", "message");

        assertEquals(ChannelType.PUSH, decorated.channel());
        assertEquals("push-1", receipt);
        assertEquals(Arrays.asList("device-1 <- message"), core.getSent()); // core still did the sending
    }

    @Test
    void stackedDecoratorsComposeInTheOrderTheyAreWrapped() {
        List<String> log = new ArrayList<>();
        FakeSender fake = new FakeSender(1);
        // Retrying outside, logging inside: logging sees every individual attempt.
        NotificationSender sender = new RetryingSender(new LoggingSender(fake, log::add), 3);

        sender.send("+1", "hi");

        assertEquals(4, log.size()); // SEND, FAIL, SEND, OK
        assertTrue(log.get(1).startsWith("FAIL"));
        assertTrue(log.get(3).startsWith("OK"));
    }
}
