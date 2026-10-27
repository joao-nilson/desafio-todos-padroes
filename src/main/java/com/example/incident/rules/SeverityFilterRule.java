package com.example.incident.rules;

import com.example.incident.alert.Alert;
import com.example.incident.model.Severity;

public class SeverityFilterRule extends AlertRule {

    private final Severity minimum;

    public SeverityFilterRule(Severity minimum) {
        this.minimum = minimum;
    }

    @Override
    protected RuleResult check(Alert alert) {
        if (alert.getSeverity().isAtLeast(minimum)) {
            return pass();
        }
        return reject("severity " + alert.getSeverity() + " is below " + minimum);
    }
}
