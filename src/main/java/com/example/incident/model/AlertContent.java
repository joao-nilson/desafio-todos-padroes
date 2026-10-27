package com.example.incident.model;

/**
 * What a channel needs to know about an alert. Channels depend on this
 * interface only, so they never see the concrete alert types (Bridge).
 */
public interface AlertContent {
    String getId();

    Severity getSeverity();

    String getHeadline();

    String getDetails();
}
