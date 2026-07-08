package com.smartHome.backend.smartPlug.chartHistory;

import java.time.Duration;

public enum TapoChartPeriod {
    LAST_HOUR(Duration.ofHours(1)),
    LAST_3_HOURS(Duration.ofHours(3)),
    LAST_24_HOURS(Duration.ofHours(24));

    private final Duration duration;

    TapoChartPeriod(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }
}
