package com.smartHome.backend.scene;

import com.smartHome.backend.scene.enums.CapabilityType;
import jakarta.persistence.*;

@Entity
@Table(name = "device_capabilities")
public class DeviceCapability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer minValue;
    private Integer maxValue;
    private String unit;

    @Enumerated(EnumType.STRING)
    private CapabilityType capability;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private Device device;

    public Long getId() {
        return id;
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

    public CapabilityType getCapability() {
        return capability;
    }

    public Device getDevice() {
        return device;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setCapability(CapabilityType capability) {
        this.capability = capability;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}