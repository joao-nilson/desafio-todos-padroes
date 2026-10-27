package com.example.incident.channel;

import com.example.incident.model.AlertContent;
import com.example.incident.model.ChannelType;

public class PushFormatter implements MessageFormatter {

    @Override
    public ChannelType channel() {
        return ChannelType.PUSH;
    }

    @Override
    public String format(AlertContent content) {
        return content.getSeverity() + ": " + content.getHeadline();
    }
}
