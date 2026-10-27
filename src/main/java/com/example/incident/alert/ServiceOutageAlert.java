package com.example.incident.alert;

import com.example.incident.channel.NotificationChannel;
import com.example.incident.model.Severity;

import java.util.List;

/** A service is down in a region. */
public class ServiceOutageAlert extends Alert {

    private final String service;
    private final String region;

    public ServiceOutageAlert(String id, String title, Severity severity, List<NotificationChannel> channels,
                              String service, String region) {
        super(id, title, severity, channels);
        this.service = service;
        this.region = region;
    }

    @Override
    public String getType() {
        return "OUTAGE";
    }

    @Override
    public String getDedupKey() {
        return service + "@" + region;
    }

    @Override
    public String getHeadline() {
        return getTitle() + ": " + service + " is down in " + region;
    }

    @Override
    public String getDetails() {
        return "Service '" + service + "' is unreachable in region " + region + ".";
    }
}
