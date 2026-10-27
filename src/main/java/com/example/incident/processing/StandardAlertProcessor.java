package com.example.incident.processing;

import com.example.incident.alert.Alert;
import com.example.incident.model.Recipient;
import com.example.incident.model.RecipientDirectory;
import com.example.incident.rules.AlertRule;
import com.example.incident.rules.RuleResult;

import java.time.Clock;
import java.util.List;

/** Validation runs the rule chain; recipients come from the directory. */
public class StandardAlertProcessor extends AlertProcessor {

    private final AlertRule chain;
    private final RecipientDirectory directory;
    private final Clock clock;

    public StandardAlertProcessor(AlertRule chain, RecipientDirectory directory, Clock clock) {
        this.chain = chain;
        this.directory = directory;
        this.clock = clock;
    }

    @Override
    protected RuleResult validate(Alert alert) {
        return chain.handle(alert);
    }

    @Override
    protected void enrich(Alert alert) {
        alert.addMetadata("processedAt", clock.instant().toString());
    }

    @Override
    protected List<Recipient> selectRecipients(Alert alert) {
        return directory.eligibleFor(alert.getSeverity());
    }
}
