package com.example.incident.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Keeps a line for every event, in order. */
public class AuditLogListener implements AlertListener {

    private final List<String> entries = new ArrayList<>();

    @Override
    public void onEvent(AlertEvent event) {
        entries.add(event.toString());
    }

    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}
