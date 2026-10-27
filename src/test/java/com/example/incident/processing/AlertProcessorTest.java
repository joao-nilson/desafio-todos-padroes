package com.example.incident.processing;

import com.example.incident.TestSupport.RecordingChannel;
import com.example.incident.alert.Alert;
import com.example.incident.model.ChannelType;
import com.example.incident.model.DeliveryResult;
import com.example.incident.model.DeliveryStatus;
import com.example.incident.model.Recipient;
import com.example.incident.model.RecipientDirectory;
import com.example.incident.model.Severity;
import com.example.incident.rules.AlertRule;
import com.example.incident.rules.DeduplicationRule;
import com.example.incident.rules.RuleChain;
import com.example.incident.rules.RuleResult;
import com.example.incident.rules.SeverityFilterRule;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.example.incident.TestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class AlertProcessorTest {

    private final RecordingChannel email = new RecordingChannel(ChannelType.EMAIL);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-27T14:00:00Z"), ZoneOffset.UTC);

    /** Subclass that logs each step so the test can check order. */
    private static class RecordingProcessor extends AlertProcessor {
        final List<String> steps = new ArrayList<>();
        final boolean valid;

        RecordingProcessor(boolean valid) {
            this.valid = valid;
        }

        @Override
        protected RuleResult validate(Alert alert) {
            steps.add("validate");
            return valid ? RuleResult.pass() : RuleResult.reject("test", "invalid");
        }

        @Override
        protected void enrich(Alert alert) {
            steps.add("enrich");
        }

        @Override
        protected List<Recipient> selectRecipients(Alert alert) {
            steps.add("selectRecipients");
            return Collections.singletonList(everywhere("ana"));
        }

        @Override
        protected List<DeliveryResult> deliver(Alert alert, List<Recipient> recipients) {
            steps.add("deliver");
            return super.deliver(alert, recipients);
        }

        @Override
        protected void recordResults(Alert alert, List<DeliveryResult> results) {
            steps.add("recordResults");
            super.recordResults(alert, results);
        }
    }

    @Test
    void workflowRunsStepsInTheDefinedOrder() {
        RecordingProcessor processor = new RecordingProcessor(true);

        ProcessingReport report = processor.process(threshold(Severity.HIGH, email));

        assertEquals(Arrays.asList("validate", "enrich", "selectRecipients", "deliver", "recordResults"),
                processor.steps);
        assertFalse(report.isRejected());
        assertEquals(1, report.getResults().size());
    }

    @Test
    void failedValidationStopsBeforeAnyOtherStep() {
        RecordingProcessor processor = new RecordingProcessor(false);

        ProcessingReport report = processor.process(threshold(Severity.HIGH, email));

        assertEquals(Collections.singletonList("validate"), processor.steps);
        assertTrue(report.isRejected());
        assertEquals("invalid", report.getRejection().getReason());
        assertTrue(email.delivered.isEmpty());
    }

    @Test
    void standardProcessorRunsChainSelectsRecipientsAndRecordsResultsOnTheAlert() {
        RecipientDirectory directory = new RecipientDirectory()
                .add(everywhere("ana"))
                .add(new Recipient("inactive", "x@example.com", null, null, Severity.LOW, false));
        AlertRule chain = RuleChain.of(new SeverityFilterRule(Severity.MEDIUM), new DeduplicationRule());
        AlertProcessor processor = new StandardAlertProcessor(chain, directory, clock);
        Alert alert = threshold(Severity.HIGH, email);

        ProcessingReport report = processor.process(alert);

        assertFalse(report.isRejected());
        assertEquals(1, report.getRecipients().size()); // inactive recipient excluded
        assertEquals(Collections.singletonList("ana"), email.recipientNames);
        assertEquals(1, alert.getDeliveryResults().size());
        assertEquals(DeliveryStatus.DELIVERED, alert.getDeliveryResults().get(0).getStatus());
        assertEquals("2026-10-27T14:00:00Z", alert.getMetadata().get("processedAt"));
    }

    @Test
    void standardProcessorRejectionDeliversNothing() {
        RecipientDirectory directory = new RecipientDirectory().add(everywhere("ana"));
        AlertProcessor processor = new StandardAlertProcessor(
                RuleChain.of(new SeverityFilterRule(Severity.HIGH)), directory, clock);
        Alert alert = threshold(Severity.LOW, email);

        ProcessingReport report = processor.process(alert);

        assertTrue(report.isRejected());
        assertEquals("SeverityFilterRule", report.getRejection().getRule());
        assertTrue(email.delivered.isEmpty());
        assertTrue(alert.getDeliveryResults().isEmpty());
        assertFalse(alert.getMetadata().containsKey("processedAt")); // enrich never ran
    }

    @Test
    void escalatingProcessorOnlyCustomizesTheEnrichStep() {
        RecipientDirectory directory = new RecipientDirectory().add(everywhere("ana"));
        AlertRule chain = RuleChain.of(new SeverityFilterRule(Severity.LOW));
        Alert alert = threshold(Severity.HIGH, email);

        new EscalatingAlertProcessor(chain, directory, clock).process(alert);

        assertEquals("true", alert.getMetadata().get("escalated"));
        assertTrue(alert.getMetadata().containsKey("processedAt")); // standard enrichment kept
        assertEquals(1, email.delivered.size());
    }
}
