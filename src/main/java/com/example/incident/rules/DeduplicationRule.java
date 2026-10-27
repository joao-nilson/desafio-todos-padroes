package com.example.incident.rules;

import com.example.incident.alert.Alert;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rejects an alert whose type + dedup key was already seen. This simple version
 * remembers keys forever; a real one would expire them. Place it last in a chain
 * so that alerts rejected by other rules are not remembered.
 */
public class DeduplicationRule extends AlertRule {

    private final Set<String> seen = ConcurrentHashMap.newKeySet();

    @Override
    protected RuleResult check(Alert alert) {
        String key = alert.getType() + ":" + alert.getDedupKey();
        if (seen.add(key)) {
            return pass();
        }
        return reject("duplicate of " + key);
    }
}
