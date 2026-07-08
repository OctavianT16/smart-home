package com.smartHome.backend.scene.dto;

import com.smartHome.backend.scene.enums.CapabilityType;
import com.smartHome.backend.scene.enums.DeviceCommandType;

import java.util.Map;

public class DeviceCommand {

    private CapabilityType capability;
    private DeviceCommandType commandType;
    private Map<String, Object> parameters;

    public CapabilityType getCapability() {
        return capability;
    }

    public void setCapability(CapabilityType capability) {
        this.capability = capability;
    }

    public DeviceCommandType getCommandType() {
        return commandType;
    }

    public void setCommand(DeviceCommandType command) {
        this.commandType = command;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}