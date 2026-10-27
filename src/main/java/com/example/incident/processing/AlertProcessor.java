package com.example.incident.processing;

import com.example.incident.alert.Alert;
import com.example.incident.model.DeliveryResult;
import com.example.incident.model.Recipient;
import com.example.incident.rules.RuleResult;

import java.util.List;

/**
 * Template Method: {@link #process} fixes the order of the workflow;
 * subclasses customize individual steps.
 */
public abstract class AlertProcessor {

    public final ProcessingReport process(Alert alert) {
        RuleResult validation = validate(alert);
        if (!validation.isPass()) {
            return ProcessingReport.rejected(alert.getId(), validation);
        }
        enrich(alert);
        List<Recipient> recipients = selectRecipients(alert);
        List<DeliveryResult> results = deliver(alert, recipients);
        recordResults(alert, results);
        return ProcessingReport.processed(alert.getId(), recipients, results);
    }

    /** Step 1: decide whether the alert may continue. */
    protected abstract RuleResult validate(Alert alert);

    /** Step 2 (optional hook): add extra information to the alert. */
    protected void enrich(Alert alert) {
    }

    /** Step 3: decide who gets notified. */
    protected abstract List<Recipient> selectRecipients(Alert alert);

    /** Step 4: send through the alert's channels. */
    protected List<DeliveryResult> deliver(Alert alert, List<Recipient> recipients) {
        return alert.dispatchTo(recipients);
    }

    /** Step 5: store results on the alert, which notifies its listeners. */
    protected void recordResults(Alert alert, List<DeliveryResult> results) {
        for (DeliveryResult result : results) {
            alert.recordDelivery(result);
        }
    }
}
