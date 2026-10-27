package com.example.incident.rules;

import com.example.incident.alert.Alert;

/**
 * Chain of Responsibility handler. A rule either rejects (the chain stops and
 * the rejection is returned) or passes the alert to the next rule.
 */
public abstract class AlertRule {

    private AlertRule next;

    /** Links the next rule and returns it, so links can be written in a row. */
    public AlertRule setNext(AlertRule next) {
        this.next = next;
        return next;
    }

    public final RuleResult handle(Alert alert) {
        RuleResult result = check(alert);
        if (!result.isPass() || next == null) {
            return result;
        }
        return next.handle(alert);
    }

    protected abstract RuleResult check(Alert alert);

    public String name() {
        return getClass().getSimpleName();
    }

    protected RuleResult pass() {
        return RuleResult.pass();
    }

    protected RuleResult reject(String reason) {
        return RuleResult.reject(name(), reason);
    }
}
