package com.example.incident.config;

import com.example.incident.model.Severity;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/** Nothing here mutates shared state, so the tests can run in any order. */
class AppConfigTest {

    @Test
    void getInstanceAlwaysReturnsTheSameObject() {
        assertSame(AppConfig.getInstance(), AppConfig.getInstance());
    }

    @Test
    void sharedInstanceHoldsTheDocumentedDefaults() {
        AppConfig config = AppConfig.getInstance();

        assertEquals(Severity.MEDIUM, config.getMinSeverity());
        assertEquals(LocalTime.of(22, 0), config.getQuietHoursStart());
        assertEquals(LocalTime.of(6, 0), config.getQuietHoursEnd());
        assertEquals(3, config.getMaxRetryAttempts());
    }

    @Test
    void customConfigsAreIndependentOfTheSharedInstance() {
        AppConfig custom = new AppConfig(Severity.CRITICAL, LocalTime.NOON, LocalTime.MIDNIGHT, 1);

        assertNotSame(AppConfig.getInstance(), custom);
        assertEquals(Severity.CRITICAL, custom.getMinSeverity());
        assertEquals(1, custom.getMaxRetryAttempts());
        assertEquals(Severity.MEDIUM, AppConfig.getInstance().getMinSeverity()); // unaffected
    }
}
