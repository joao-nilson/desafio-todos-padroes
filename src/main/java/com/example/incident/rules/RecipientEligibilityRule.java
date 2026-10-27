package com.example.incident.rules;

import com.example.incident.alert.Alert;
import com.example.incident.model.RecipientDirectory;

/** Rejects the alert if nobody is eligible to receive it. */
public class RecipientEligibilityRule extends AlertRule {

    private final RecipientDirectory directory;

    public RecipientEligibilityRule(RecipientDirectory directory) {
        this.directory = directory;
    }

    @Override
    protected RuleResult check(Alert alert) {
        if (directory.eligibleFor(alert.getSeverity()).isEmpty()) {
            return reject("no eligible recipients for severity " + alert.getSeverity());
        }
        return pass();
    }
}
