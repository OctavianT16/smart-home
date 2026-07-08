package com.smartHome.backend.dehumidifier;

import java.util.Arrays;

public enum TuyaMode {
    MANUAL("manual"),
    AUTO("auto"),
    LAUNDRY("laundry"),
    PURIFY("purify"),
    SLEEP("sleep");

    private final String value;

    TuyaMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TuyaMode fromValue(String value) {
        return Arrays.stream(values())
                .filter(mode -> mode.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mod Tuya invalid: " + value));
    }
}