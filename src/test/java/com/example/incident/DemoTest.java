package com.example.incident;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DemoTest {

    @Test
    void demoRunsEndToEndAndShowsEachBehavior() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        Demo.run(new PrintStream(buffer, true, StandardCharsets.UTF_8));

        String output = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("EMAIL -> Ana: DELIVERED"));
        assertTrue(output.contains("[sms-log] FAIL"));                       // decorator: first SMS failed...
        assertTrue(output.contains("SMS -> Ana: DELIVERED"));                // ...and the retry succeeded
        assertTrue(output.contains("REJECTED by DeduplicationRule"));
        assertTrue(output.contains("REJECTED by SeverityFilterRule"));
        assertTrue(output.contains("Cannot acknowledge an alert that is RESOLVED"));
        assertTrue(output.contains("STATUS_CHANGED alert=A-1 NEW -> ACKNOWLEDGED"));
    }
}
