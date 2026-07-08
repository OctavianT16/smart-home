package com.smartHome.backend.scene.dto;

import com.smartHome.backend.scene.enums.CapabilityType;

public class DeviceCapabilityResponse {

    private final Long id;
    private final CapabilityType capability;
    private final Integer minValue;
    private final Integer maxValue;
    private final String unit;

    public DeviceCapabilityResponse(
            Long id,
            CapabilityType capability,
            Integer minValue,
            Integer maxValue,
            String unit
    ) {
        this.id = id;
        this.capability = capability;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
    }

    public Long getId() {
        return id;
    }

    public CapabilityType getCapability() {
        return capability;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public String getUnit() {
        return unit;
    }
}