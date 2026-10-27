package com.example.incident.rules;

public final class RuleResult {

    private final boolean pass;
    private final String rule;
    private final String reason;

    private RuleResult(boolean pass, String rule, String reason) {
        this.pass = pass;
        this.rule = rule;
        this.reason = reason;
    }

    public static RuleResult pass() {
        return new RuleResult(true, null, null);
    }

    public static RuleResult reject(String rule, String reason) {
        return new RuleResult(false, rule, reason);
    }

    public boolean isPass() {
        return pass;
    }

    /** Name of the rule that rejected the alert; null if it passed. */
    public String getRule() {
        return rule;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return pass ? "PASS" : "REJECTED by " + rule + ": " + reason;
    }
}
