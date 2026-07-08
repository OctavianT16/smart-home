package com.smartHome.backend.scene.dto;

import com.smartHome.backend.scene.enums.CapabilityType;
import com.smartHome.backend.scene.enums.DeviceCommandType;

import java.util.Map;

public class SceneActionRequest {

    private Long deviceId;
    private CapabilityType capability;
    private DeviceCommandType commandType;
    private Map<String, Object> parameters;
    private Integer executionOrder;
    private Integer delayMs;

    public Long getDeviceId() {
        return deviceId;
    }

    public CapabilityType getCapability() {
        return capability;
    }

    public DeviceCommandType getCommandType() {
        return commandType;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Integer getExecutionOrder() {
        return executionOrder;
    }

    public Integer getDelayMs() {
        return delayMs;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public void setCapability(CapabilityType capability) {
        this.capability = capability;
    }

    public void setCommandType(DeviceCommandType command) {
        this.commandType = command;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setExecutionOrder(Integer executionOrder) {
        this.executionOrder = executionOrder;
    }

    public void setDelayMs(Integer delayMs) {
        this.delayMs = delayMs;
    }
}