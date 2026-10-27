package com.example.incident.state;

/** Terminal state: no transitions allowed. */
public class ResolvedState extends AlertState {

    @Override
    public AlertStatus status() {
        return AlertStatus.RESOLVED;
    }
}
