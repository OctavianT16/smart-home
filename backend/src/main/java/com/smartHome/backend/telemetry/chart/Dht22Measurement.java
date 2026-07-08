package com.smartHome.backend.telemetry.chart;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dht22_measurements")
public class Dht22Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;

    private String sensor;

    private Double temperatureC;

    private Double humidityPct;

    /**
     * Timestamp primit de la ESP32, dacă există.
     */
    private Long deviceTimestampMs;

    /**
     * Momentul real când backend-ul a salvat măsurătoarea.
     */
    private Instant recordedAt;

    public Dht22Measurement() {
    }

    public Dht22Measurement(
            String deviceId,
            String sensor,
            Double temperatureC,
            Double humidityPct,
            Long deviceTimestampMs,
            Instant recordedAt
    ) {
        this.deviceId = deviceId;
        this.sensor = sensor;
        this.temperatureC = temperatureC;
        this.humidityPct = humidityPct;
        this.deviceTimestampMs = deviceTimestampMs;
        this.recordedAt = recordedAt;
    }

    public Long getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getSensor() {
        return sensor;
    }

    public Double getTemperatureC() {
        return temperatureC;
    }

    public Double getHumidityPct() {
        return humidityPct;
    }

    public Long getDeviceTimestampMs() {
        return deviceTimestampMs;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
