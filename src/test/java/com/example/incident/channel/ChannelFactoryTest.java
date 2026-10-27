package com.example.incident.channel;

import com.example.incident.alert.Alert;
import com.example.incident.model.ChannelType;
import com.example.incident.model.DeliveryResult;
import com.example.incident.model.DeliveryStatus;
import com.example.incident.model.Recipient;
import com.example.incident.model.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.example.incident.TestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class ChannelFactoryTest {

    private static ChannelFactory factoryFor(ChannelType type) {
        switch (type) {
            case EMAIL:
                return new EmailChannelFactory();
            case SMS:
                return new SmsChannelFactory();
            default:
                return new PushChannelFactory();
        }
    }

    @ParameterizedTest
    @EnumSource(ChannelType.class)
    void eachFactoryReturnsComponentsForItsOwnChannel(ChannelType type) {
        ChannelFactory factory = factoryFor(type);

        assertEquals(type, factory.createSender().channel());
        assertEquals(type, factory.createFormatter().channel());
        assertEquals(type, factory.createStatusChecker().channel());
    }

    @Test
    void channelRejectsComponentsFromDifferentFamilies() {
        NotificationSender emailSender = new EmailChannelFactory().createSender();
        MessageFormatter smsFormatter = new SmsChannelFactory().createFormatter();
        DeliveryStatusChecker emailChecker = new EmailChannelFactory().createStatusChecker();

        assertThrows(IllegalArgumentException.class,
                () -> new StandardChannel(emailSender, smsFormatter, emailChecker));
    }

    @Test
    void checkerOnlyRecognizesReceiptsFromItsOwnChannel() {
        DeliveryStatusChecker smsChecker = new SmsChannelFactory().createStatusChecker();
        String smsReceipt = new SmsChannelFactory().createSender().send("+1", "hi");

        assertEquals(DeliveryStatus.DELIVERED, smsChecker.check(smsReceipt));
        assertEquals(DeliveryStatus.UNKNOWN, smsChecker.check("email-1"));
        assertEquals(DeliveryStatus.UNKNOWN, smsChecker.check(null));
    }

    @Test
    void formattersShapeTheSameAlertDifferently() {
        Alert alert = threshold(Severity.HIGH, new RecordingChannel(ChannelType.EMAIL));

        assertTrue(new EmailFormatter().format(alert).startsWith("Subject: [HIGH]"));
        assertTrue(new EmailFormatter().format(alert).contains(alert.getDetails()));
        assertEquals("HIGH: " + alert.getHeadline(), new PushFormatter().format(alert));
        assertFalse(new SmsFormatter().format(alert).contains(alert.getDetails()));
    }

    @Test
    void smsFormatterTruncatesLongMessages() {
        StringBuilder longMetric = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longMetric.append('x');
        }
        Alert alert = threshold("A-1", longMetric.toString(), Severity.HIGH, new RecordingChannel(ChannelType.SMS));

        assertEquals(160, new SmsFormatter().format(alert).length());
    }

    @Test
    void channelBuiltFromFactoryDeliversAndConfirmsStatus() {
        NotificationChannel channel = StandardChannel.from(new EmailChannelFactory());
        Alert alert = threshold(Severity.HIGH, channel);

        DeliveryResult result = channel.deliver(alert, everywhere("ana"));

        assertEquals(DeliveryStatus.DELIVERED, result.getStatus());
        assertEquals(ChannelType.EMAIL, result.getChannel());
        assertTrue(result.getDetail().startsWith("email-"));
    }

    @Test
    void recipientWithoutAddressIsSkipped() {
        NotificationChannel channel = StandardChannel.from(new SmsChannelFactory());
        Recipient noPhone = new Recipient("bob", "bob@example.com", null, "bob-device", Severity.LOW);

        DeliveryResult result = channel.deliver(threshold(Severity.HIGH, channel), noPhone);

        assertEquals(DeliveryStatus.SKIPPED, result.getStatus());
    }

    @Test
    void senderFailureBecomesFailedResultInsteadOfException() {
        SmsChannelFactory flaky = new SmsChannelFactory(1);
        NotificationChannel channel = StandardChannel.from(flaky);

        DeliveryResult result = channel.deliver(threshold(Severity.HIGH, channel), everywhere("ana"));

        assertEquals(DeliveryStatus.FAILED, result.getStatus());
    }
}
