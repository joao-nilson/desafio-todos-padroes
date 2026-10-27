package com.example.incident.channel;

import com.example.incident.model.AlertContent;
import com.example.incident.model.ChannelType;

public class SmsFormatter implements MessageFormatter {

    static final int MAX_LENGTH = 160;

    @Override
    public ChannelType channel() {
        return ChannelType.SMS;
    }

    @Override
    public String format(AlertContent content) {
        String text = "[" + content.getSeverity() + "] " + content.getHeadline();
        return text.length() <= MAX_LENGTH ? text : text.substring(0, MAX_LENGTH);
    }
}
