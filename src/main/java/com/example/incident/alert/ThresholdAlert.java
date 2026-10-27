package com.example.incident.alert;

import com.example.incident.channel.NotificationChannel;
import com.example.incident.model.Severity;

import java.util.List;

/** A metric crossed a limit, e.g. CPU at 97% (limit 90%). */
public class ThresholdAlert extends Alert {

    private final String metric;
    private final double value;
    private final double threshold;

    public ThresholdAlert(String id, String title, Severity severity, List<NotificationChannel> channels,
                          String metric, double value, double threshold) {
        super(id, title, severity, channels);
        this.metric = metric;
        this.value = value;
        this.threshold = threshold;
    }

    @Override
    public String getType() {
        return "THRESHOLD";
    }

    @Override
    public String getDedupKey() {
        return metric;
    }

    @Override
    public String getHeadline() {
        return getTitle() + ": " + metric + " at " + value;
    }

    @Override
    public String getDetails() {
        return "Metric '" + metric + "' is " + value + ", above the limit of " + threshold + ".";
    }
}
