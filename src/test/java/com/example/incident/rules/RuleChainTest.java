package com.example.incident.rules;

import com.example.incident.TestSupport.RecordingChannel;
import com.example.incident.alert.Alert;
import com.example.incident.model.ChannelType;
import com.example.incident.model.Recipient;
import com.example.incident.model.RecipientDirectory;
import com.example.incident.model.Severity;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static com.example.incident.TestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class RuleChainTest {

    private final RecordingChannel email = new RecordingChannel(ChannelType.EMAIL);

    private static Clock clockAt(String time) {
        return Clock.fixed(Instant.parse("2026-10-27T" + time + ":00Z"), ZoneOffset.UTC);
    }

    /** Test double: passes or rejects on demand and records that it was called. */
    private static class ScriptedRule extends AlertRule {
        private final String label;
        private final boolean pass;
        private final List<String> calls;

        ScriptedRule(String label, boolean pass, List<String> calls) {
            this.label = label;
            this.pass = pass;
            this.calls = calls;
        }

        @Override
        protected RuleResult check(Alert alert) {
            calls.add(label);
            return pass ? pass() : reject(label + " said no");
        }
    }

    // ----- chain behavior -----

    @Test
    void passingRulesAllRunInOrderAndAlertPasses() {
        List<String> calls = new ArrayList<>();
        AlertRule chain = RuleChain.of(
                new ScriptedRule("first", true, calls),
                new ScriptedRule("second", true, calls),
                new ScriptedRule("third", true, calls));

        RuleResult result = chain.handle(threshold(Severity.HIGH, email));

        assertTrue(result.isPass());
        assertEquals(java.util.Arrays.asList("first", "second", "third"), calls);
    }

    @Test
    void rejectionStopsTheChainAndNamesTheRule() {
        List<String> calls = new ArrayList<>();
        AlertRule chain = RuleChain.of(
                new ScriptedRule("first", true, calls),
                new ScriptedRule("second", false, calls),
                new ScriptedRule("third", true, calls));

        RuleResult result = chain.handle(threshold(Severity.HIGH, email));

        assertFalse(result.isPass());
        assertEquals("ScriptedRule", result.getRule());
        assertEquals("second said no", result.getReason());
        assertEquals(java.util.Arrays.asList("first", "second"), calls); // third never ran
    }

    @Test
    void emptyChainIsRejectedAtConfigurationTime() {
        assertThrows(IllegalArgumentException.class, RuleChain::of);
    }

    @Test
    void reorderingTheArgumentsReordersTheChain() {
        List<String> calls = new ArrayList<>();
        AlertRule chain = RuleChain.of(
                new ScriptedRule("b", true, calls),
                new ScriptedRule("a", true, calls));

        chain.handle(threshold(Severity.HIGH, email));

        assertEquals(java.util.Arrays.asList("b", "a"), calls);
    }

    // ----- individual rules -----

    @Test
    void severityFilterPassesAtOrAboveMinimumAndRejectsBelow() {
        AlertRule rule = new SeverityFilterRule(Severity.MEDIUM);

        assertTrue(rule.handle(threshold(Severity.MEDIUM, email)).isPass());
        assertTrue(rule.handle(threshold(Severity.CRITICAL, email)).isPass());
        assertFalse(rule.handle(threshold(Severity.LOW, email)).isPass());
    }

    @Test
    void quietHoursRejectsInsideWindowThatCrossesMidnight() {
        LocalTime start = LocalTime.of(22, 0);
        LocalTime end = LocalTime.of(6, 0);
        Alert alert = threshold(Severity.HIGH, email);

        assertFalse(new QuietHoursRule(clockAt("23:30"), start, end).handle(alert).isPass());
        assertFalse(new QuietHoursRule(clockAt("03:00"), start, end).handle(alert).isPass());
        assertTrue(new QuietHoursRule(clockAt("14:00"), start, end).handle(alert).isPass());
        assertTrue(new QuietHoursRule(clockAt("06:00"), start, end).handle(alert).isPass()); // end is exclusive
    }

    @Test
    void quietHoursLetsCriticalAlertsThrough() {
        AlertRule rule = new QuietHoursRule(clockAt("23:30"), LocalTime.of(22, 0), LocalTime.of(6, 0));

        assertTrue(rule.handle(threshold(Severity.CRITICAL, email)).isPass());
    }

    @Test
    void quietHoursWindowWithinOneDayWorks() {
        AlertRule rule = new QuietHoursRule(clockAt("13:00"), LocalTime.of(12, 0), LocalTime.of(14, 0));

        assertFalse(rule.handle(threshold(Severity.HIGH, email)).isPass());
    }

    @Test
    void deduplicationRejectsSecondAlertForSameIncidentOnly() {
        AlertRule rule = new DeduplicationRule();

        assertTrue(rule.handle(threshold("A-1", "cpu_usage", Severity.HIGH, email)).isPass());
        RuleResult duplicate = rule.handle(threshold("A-2", "cpu_usage", Severity.HIGH, email));
        assertFalse(duplicate.isPass());
        assertTrue(duplicate.getReason().contains("cpu_usage"));
        assertTrue(rule.handle(threshold("A-3", "disk_usage", Severity.HIGH, email)).isPass());
        // same key but a different alert type is a different incident
        assertTrue(rule.handle(outage("A-4", Severity.HIGH, email)).isPass());
    }

    @Test
    void eligibilityRejectsWhenNobodyCanReceiveTheAlert() {
        RecipientDirectory directory = new RecipientDirectory()
                .add(new Recipient("inactive", "i@example.com", null, null, Severity.LOW, false))
                .add(new Recipient("picky", "p@example.com", null, null, Severity.CRITICAL));
        AlertRule rule = new RecipientEligibilityRule(directory);

        assertFalse(rule.handle(threshold(Severity.HIGH, email)).isPass());   // inactive + too picky
        assertTrue(rule.handle(threshold(Severity.CRITICAL, email)).isPass()); // picky accepts CRITICAL
    }

    // ----- a realistic chain -----

    @Test
    void realChainRejectsAtTheFirstFailingRuleWithoutRememberingTheAlert() {
        RecipientDirectory directory = new RecipientDirectory().add(everywhere("ana"));
        DeduplicationRule dedup = new DeduplicationRule();
        AlertRule chain = RuleChain.of(
                new SeverityFilterRule(Severity.MEDIUM),
                new RecipientEligibilityRule(directory),
                dedup);

        RuleResult low = chain.handle(threshold("A-1", "cpu_usage", Severity.LOW, email));
        assertEquals("SeverityFilterRule", low.getRule());

        // The LOW alert was stopped before dedup, so the same incident can still pass later.
        assertTrue(chain.handle(threshold("A-2", "cpu_usage", Severity.HIGH, email)).isPass());
        assertEquals("DeduplicationRule", chain.handle(threshold("A-3", "cpu_usage", Severity.HIGH, email)).getRule());
    }
}
