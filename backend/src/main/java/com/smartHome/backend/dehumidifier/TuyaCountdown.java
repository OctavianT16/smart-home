package com.smartHome.backend.dehumidifier;
import java.util.Arrays;

public enum TuyaCountdown {
    CANCEL("cancel"),
    ONE_HOUR("1h"),
    TWO_HOURS("2h"),
    FOUR_HOURS("4h"),
    SIX_HOURS("6h"),
    EIGHT_HOURS("8h");

    private final String value;

    TuyaCountdown(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TuyaCountdown fromValue(String value) {
        return Arrays.stream(values())
                .filter(countdown -> countdown.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Timer invalid: " + value));
    }
}
