package com.smartHome.backend.scene.dto;

import com.smartHome.backend.scene.enums.CapabilityType;
import com.smartHome.backend.scene.enums.DeviceCommandType;

import java.util.Map;

public class SceneActionResponse {

    private final Long id;
    private final Integer executionOrder;
    private final Integer delayMs;
    private final Long deviceId;
    private final String deviceName;
    private final CapabilityType capability;
    private final DeviceCommandType command;
    private final Map<String, Object> parameters;

    public SceneActionResponse(
            Long id,
            Integer executionOrder,
            Integer delayMs,
            Long deviceId,
            String deviceName,
            CapabilityType capability,
            DeviceCommandType command,
            Map<String, Object> parameters
    ) {
        this.id = id;
        this.executionOrder = executionOrder;
        this.delayMs = delayMs;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.capability = capability;
        this.command = command;
        this.parameters = parameters;
    }

    public Long getId() {
        return id;
    }

    public Integer getExecutionOrder() {
        return executionOrder;
    }

    public Integer getDelayMs() {
        return delayMs;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public CapabilityType getCapability() {
        return capability;
    }

    public DeviceCommandType getCommand() {
        return command;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}