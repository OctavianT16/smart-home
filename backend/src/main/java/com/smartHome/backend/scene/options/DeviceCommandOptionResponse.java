package com.smartHome.backend.scene.options;

import com.smartHome.backend.scene.enums.CapabilityType;
import com.smartHome.backend.scene.enums.DeviceCommandType;

import java.util.List;

public class DeviceCommandOptionResponse {

    private final CapabilityType capability;
    private final DeviceCommandType command;
    private final String label;
    private final List<CommandParameterResponse> parameters;

    public DeviceCommandOptionResponse(
            CapabilityType capability,
            DeviceCommandType command,
            String label,
            List<CommandParameterResponse> parameters
    ) {
        this.capability = capability;
        this.command = command;
        this.label = label;
        this.parameters = parameters;
    }

    public CapabilityType getCapability() {
        return capability;
    }

    public DeviceCommandType getCommand() {
        return command;
    }

    public String getLabel() {
        return label;
    }

    public List<CommandParameterResponse> getParameters() {
        return parameters;
    }
}