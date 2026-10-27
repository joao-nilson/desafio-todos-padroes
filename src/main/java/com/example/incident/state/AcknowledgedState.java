package com.example.incident.state;

public class AcknowledgedState extends AlertState {

    @Override
    public AlertStatus status() {
        return AlertStatus.ACKNOWLEDGED;
    }

    @Override
    public AlertState resolve() {
        return new ResolvedState();
    }
}
