package com.example.incident.processing;

import com.example.incident.model.DeliveryResult;
import com.example.incident.model.Recipient;
import com.example.incident.rules.RuleResult;

import java.util.Collections;
import java.util.List;

public class ProcessingReport {

    private final String alertId;
    private final RuleResult rejection;
    private final List<Recipient> recipients;
    private final List<DeliveryResult> results;

    private ProcessingReport(String alertId, RuleResult rejection,
                             List<Recipient> recipients, List<DeliveryResult> results) {
        this.alertId = alertId;
        this.rejection = rejection;
        this.recipients = recipients;
        this.results = results;
    }

    public static ProcessingReport rejected(String alertId, RuleResult rejection) {
        return new ProcessingReport(alertId, rejection,
                Collections.<Recipient>emptyList(), Collections.<DeliveryResult>emptyList());
    }

    public static ProcessingReport processed(String alertId, List<Recipient> recipients, List<DeliveryResult> results) {
        return new ProcessingReport(alertId, null, recipients, results);
    }

    public String getAlertId() {
        return alertId;
    }

    public boolean isRejected() {
        return rejection != null;
    }

    /** Null when the alert was processed. */
    public RuleResult getRejection() {
        return rejection;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public List<DeliveryResult> getResults() {
        return results;
    }
}
