package com.example.incident.state;

public class InvalidTransitionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidTransitionException(AlertStatus from, String action) {
        super("Cannot " + action + " an alert that is " + from);
    }
}
