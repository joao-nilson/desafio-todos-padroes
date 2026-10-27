package com.example.incident.model;

public class Recipient {

    private final String name;
    private final String email;
    private final String phone;
    private final String deviceToken;
    private final Severity minSeverity;
    private final boolean active;

    public Recipient(String name, String email, String phone, String deviceToken, Severity minSeverity) {
        this(name, email, phone, deviceToken, minSeverity, true);
    }

    public Recipient(String name, String email, String phone, String deviceToken,
                     Severity minSeverity, boolean active) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.deviceToken = deviceToken;
        this.minSeverity = minSeverity;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    /** Returns the address this recipient has for the channel, or null if none. */
    public String addressFor(ChannelType type) {
        switch (type) {
            case EMAIL:
                return email;
            case SMS:
                return phone;
            case PUSH:
                return deviceToken;
            default:
                throw new IllegalArgumentException("Unknown channel: " + type);
        }
    }

    public boolean isEligibleFor(Severity severity) {
        return active && severity.isAtLeast(minSeverity);
    }
}
