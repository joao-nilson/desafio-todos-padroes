package com.example.incident.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** In-memory list of people who can be notified. */
public class RecipientDirectory {

    private final List<Recipient> recipients = new ArrayList<>();

    public RecipientDirectory add(Recipient recipient) {
        recipients.add(recipient);
        return this;
    }

    public List<Recipient> eligibleFor(Severity severity) {
        return recipients.stream()
                .filter(r -> r.isEligibleFor(severity))
                .collect(Collectors.toList());
    }
}
