package com.smartHome.backend.dehumidifier;

public class DehumidifierStatusResponse {

    private Boolean power;
    private String mode;
    private Integer humidity;
    private String fanSpeed;
    private String countdown;

    public DehumidifierStatusResponse(
            Boolean power,
            String mode,
            Integer humidity,
            String fanSpeed,
            String countdown
    ) {
        this.power = power;
        this.mode = mode;
        this.humidity = humidity;
        this.fanSpeed = fanSpeed;
        this.countdown = countdown;
    }

    public Boolean getPower() {
        return power;
    }

    public String getMode() {
        return mode;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public String getFanSpeed() {
        return fanSpeed;
    }

    public String getCountdown() {
        return countdown;
    }
}