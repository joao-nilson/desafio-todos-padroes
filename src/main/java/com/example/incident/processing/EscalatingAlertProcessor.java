package com.example.incident.processing;

import com.example.incident.alert.Alert;
import com.example.incident.model.RecipientDirectory;
import com.example.incident.rules.AlertRule;

import java.time.Clock;

/** Same workflow as the standard one, but marks the alert as escalated during enrichment. */
public class EscalatingAlertProcessor extends StandardAlertProcessor {

    public EscalatingAlertProcessor(AlertRule chain, RecipientDirectory directory, Clock clock) {
        super(chain, directory, clock);
    }

    @Override
    protected void enrich(Alert alert) {
        super.enrich(alert);
        alert.addMetadata("escalated", "true");
    }
}
