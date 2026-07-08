package com.smartHome.backend.climate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AcDtos {

    public record PowerRequest(@NotNull Boolean on) {}

    public enum Mode {
        AUTO, COOL, HEAT, FAN, DRY
    }

    public record ModeRequest(@NotNull Mode mode) {}

    public record TemperatureRequest(
            @NotNull @Min(16) @Max(30) Integer celsius
    ) {}


    public record FanRequest(
            @NotNull @Min(1) @Max(5) Integer level
    ) {}

    public record AutoRequest(@NotNull Boolean enabled) {}

    public record targetTemperatureRequest(@NotNull Integer targetCelsius) {}


}
