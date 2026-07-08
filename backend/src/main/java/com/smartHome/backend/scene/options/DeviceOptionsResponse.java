package com.smartHome.backend.scene.options;

import com.smartHome.backend.scene.enums.DeviceType;
import com.smartHome.backend.scene.enums.IntegrationType;

import java.util.List;

public class DeviceOptionsResponse {

    private final Long deviceId;
    private final String deviceName;
    private final DeviceType deviceType;
    private final IntegrationType integrationType;
    private final List<DeviceCommandOptionResponse> actions;

    public DeviceOptionsResponse(
            Long deviceId,
            String deviceName,
            DeviceType deviceType,
            IntegrationType integrationType,
            List<DeviceCommandOptionResponse> actions
    ) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.integrationType = integrationType;
        this.actions = actions;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public List<DeviceCommandOptionResponse> getActions() {
        return actions;
    }
}