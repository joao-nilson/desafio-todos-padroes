package com.example.incident.rules;

import com.example.incident.alert.Alert;
import com.example.incident.model.Severity;

import java.time.Clock;
import java.time.LocalTime;

/** Rejects non-critical alerts during quiet hours. The window may cross midnight (e.g. 22:00-06:00). */
public class QuietHoursRule extends AlertRule {

    private final Clock clock;
    private final LocalTime start;
    private final LocalTime end;

    public QuietHoursRule(Clock clock, LocalTime start, LocalTime end) {
        this.clock = clock;
        this.start = start;
        this.end = end;
    }

    @Override
    protected RuleResult check(Alert alert) {
        if (alert.getSeverity() == Severity.CRITICAL) {
            return pass();
        }
        LocalTime now = LocalTime.now(clock);
        if (isQuiet(now)) {
            return reject("quiet hours (" + start + "-" + end + ")");
        }
        return pass();
    }

    private boolean isQuiet(LocalTime now) {
        if (start.equals(end)) {
            return false;
        }
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        return !now.isBefore(start) || now.isBefore(end);
    }
}
