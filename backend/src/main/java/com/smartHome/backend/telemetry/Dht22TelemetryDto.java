package com.smartHome.backend.telemetry;

public class Dht22TelemetryDto {
    public String deviceId;
    public String sensor;
    public Double temperatureC;
    public Double humidityPct;
    public Long ts;

    @Override
    public String toString() {
        return "Dht22TelemetryDto{" +
                "deviceId='" + deviceId + '\'' +
                ", sensor='" + sensor + '\'' +
                ", temperatureC=" + temperatureC +
                ", humidityPct=" + humidityPct +
                ", ts=" + ts +
                '}';
    }
}
