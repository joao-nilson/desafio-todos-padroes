package com.example.incident.channel;

import com.example.incident.model.AlertContent;
import com.example.incident.model.ChannelType;

public class EmailFormatter implements MessageFormatter {

    @Override
    public ChannelType channel() {
        return ChannelType.EMAIL;
    }

    @Override
    public String format(AlertContent content) {
        return "Subject: [" + content.getSeverity() + "] " + content.getHeadline()
                + "\n\n" + content.getDetails();
    }
}
