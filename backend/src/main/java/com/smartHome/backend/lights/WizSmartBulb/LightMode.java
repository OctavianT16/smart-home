package com.smartHome.backend.lights.WizSmartBulb;

import java.util.Arrays;
import java.util.Optional;

public enum LightMode {
    JUNGLE(24, true),
    TV_TIME(18, false),
    RELAX(16, false),
    SUMMER(21, true);

    public final int sceneId;
    public final boolean needsSpeed;

    LightMode(int sceneId, boolean needsSpeed) {
        this.sceneId = sceneId;
        this.needsSpeed = needsSpeed;
    }

    public static LightMode fromString(String s) {
        if (s == null) throw new IllegalArgumentException("Mode is null");
        return switch (s.trim().toLowerCase()) {
            case "jungle" -> JUNGLE;
            case "tv", "tvtime", "tv time", "tv_time", "tv-time" -> TV_TIME;
            case "relax" -> RELAX;
            case "summer" -> SUMMER;
            default -> throw new IllegalArgumentException("Mod necunoscut: " + s);
        };
    }
    public static Optional<LightMode> fromSceneId(int sceneId) {
        return Arrays.stream(values())
                .filter(mode -> mode.sceneId == sceneId)
                .findFirst();
    }
}
