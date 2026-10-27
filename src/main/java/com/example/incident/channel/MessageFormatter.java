package com.example.incident.channel;

import com.example.incident.model.AlertContent;
import com.example.incident.model.ChannelType;

public interface MessageFormatter {

    ChannelType channel();

    String format(AlertContent content);
}
