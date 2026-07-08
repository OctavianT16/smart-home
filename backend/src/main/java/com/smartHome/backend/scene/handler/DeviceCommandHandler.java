package com.smartHome.backend.scene.handler;

import com.smartHome.backend.scene.dto.DeviceCommand;
import com.smartHome.backend.scene.Device;

public interface DeviceCommandHandler {

    boolean supports(Device device, DeviceCommand command);

    void execute(Device device, DeviceCommand command);
}