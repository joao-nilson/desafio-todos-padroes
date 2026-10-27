package com.example.incident.state;

/**
 * State pattern: every transition is rejected by default; each concrete state
 * overrides only the transitions it allows, and returns the next state.
 */
public abstract class AlertState {

    public abstract AlertStatus status();

    public AlertState acknowledge() {
        throw new InvalidTransitionException(status(), "acknowledge");
    }

    public AlertState resolve() {
        throw new InvalidTransitionException(status(), "resolve");
    }
}
