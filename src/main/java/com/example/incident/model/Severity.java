package com.example.incident.model;

public enum Severity {
    LOW, MEDIUM, HIGH, CRITICAL;

    public boolean isAtLeast(Severity other) {
        return compareTo(other) >= 0;
    }
}
