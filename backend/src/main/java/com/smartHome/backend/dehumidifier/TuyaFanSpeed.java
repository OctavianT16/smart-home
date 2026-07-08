package com.smartHome.backend.dehumidifier;
import java.util.Arrays;

public enum TuyaFanSpeed {
    LOW("low"),
    MID("mid"),
    HIGH("high");

    private final String value;

    TuyaFanSpeed(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TuyaFanSpeed fromValue(String value) {
        return Arrays.stream(values())
                .filter(speed -> speed.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Viteză ventilator invalidă: " + value));
    }
}
