package com.smartHome.backend.telemetry.chart;

public record Dht22HistoryPointDto(
        Long ts,
        Double temperatureC,
        Double humidityPct
) {
}