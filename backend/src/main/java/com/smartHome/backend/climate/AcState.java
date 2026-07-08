package com.smartHome.backend.climate;

public record AcState(
        boolean power,
        AcDtos.Mode mode,
        int temperatureC,
        int fanLevel,
        boolean autoEnabled,
        int targetAmbientTemperatureC,
        String lastUpdatedBy
) {
    public static AcState defaults() {
        return new AcState(false,
                AcDtos.Mode.COOL,
                24,
                3,
                false,
                24,
                "startup");
    }

    public AcState withPower(boolean power, String updatedBy) {
        return new AcState(
                power,
                mode,
                temperatureC,
                fanLevel,
                autoEnabled,
                targetAmbientTemperatureC,
                updatedBy
        );
    }

    public AcState withMode(AcDtos.Mode mode, String updatedBy) {
        return new AcState(
                power,
                mode,
                temperatureC,
                fanLevel,
                autoEnabled,
                targetAmbientTemperatureC,
                updatedBy
        );
    }

    public AcState withTemperatureC(int temperatureC, String updatedBy) {
        return new AcState(
                power,
                mode,
                temperatureC,
                fanLevel,
                autoEnabled,
                targetAmbientTemperatureC,
                updatedBy
        );
    }

    public AcState withFanLevel(int fanLevel, String updatedBy) {
        return new AcState(
                power,
                mode,
                temperatureC,
                fanLevel,
                autoEnabled,
                targetAmbientTemperatureC,
                updatedBy
        );
    }

    public AcState withAutoEnabled(boolean autoEnabled, String updatedBy) {
        return new AcState(
                power,
                mode,
                temperatureC,
                fanLevel,
                autoEnabled,
                targetAmbientTemperatureC,
                updatedBy
        );
    }

    public AcState withTargetAmbientTemperatureC(int targetAmbientTemperatureC, String updatedBy) {
        return new AcState(
                power,
                mode,
                temperatureC,
                fanLevel,
                autoEnabled,
                targetAmbientTemperatureC,
                updatedBy
        );
    }




}