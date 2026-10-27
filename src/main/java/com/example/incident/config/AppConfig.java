package com.example.incident.config;

import com.example.incident.model.Severity;

import java.time.LocalTime;

/**
 * Singleton: the one shared set of application defaults, built once and never changed.
 * It is immutable on purpose, so sharing it needs no locking and tests cannot affect
 * each other through it. Components never call getInstance() themselves; the
 * composition root reads it and passes the values in. The public constructor lets
 * tests build their own configs.
 */
public final class AppConfig {

    private static final class Holder {
        static final AppConfig INSTANCE =
                new AppConfig(Severity.MEDIUM, LocalTime.of(22, 0), LocalTime.of(6, 0), 3);
    }

    private final Severity minSeverity;
    private final LocalTime quietHoursStart;
    private final LocalTime quietHoursEnd;
    private final int maxRetryAttempts;

    public AppConfig(Severity minSeverity, LocalTime quietHoursStart, LocalTime quietHoursEnd, int maxRetryAttempts) {
        this.minSeverity = minSeverity;
        this.quietHoursStart = quietHoursStart;
        this.quietHoursEnd = quietHoursEnd;
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public static AppConfig getInstance() {
        return Holder.INSTANCE;
    }

    public Severity getMinSeverity() {
        return minSeverity;
    }

    public LocalTime getQuietHoursStart() {
        return quietHoursStart;
    }

    public LocalTime getQuietHoursEnd() {
        return quietHoursEnd;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }
}
