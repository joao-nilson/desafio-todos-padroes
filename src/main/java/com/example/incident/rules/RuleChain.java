package com.example.incident.rules;

/** Configures a chain from an ordered list: {@code RuleChain.of(a, b, c)} runs a, then b, then c. */
public final class RuleChain {

    private RuleChain() {
    }

    public static AlertRule of(AlertRule... rules) {
        if (rules.length == 0) {
            throw new IllegalArgumentException("A chain needs at least one rule");
        }
        for (int i = 0; i < rules.length - 1; i++) {
            rules[i].setNext(rules[i + 1]);
        }
        return rules[0];
    }
}
