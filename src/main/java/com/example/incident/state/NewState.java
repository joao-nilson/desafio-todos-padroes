package com.example.incident.state;

public class NewState extends AlertState {

    @Override
    public AlertStatus status() {
        return AlertStatus.NEW;
    }

    @Override
    public AlertState acknowledge() {
        return new AcknowledgedState();
    }

    @Override
    public AlertState resolve() {
        return new ResolvedState();
    }
}
