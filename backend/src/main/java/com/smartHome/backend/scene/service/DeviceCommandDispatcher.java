package com.smartHome.backend.scene.service;

import com.smartHome.backend.scene.dto.DeviceCommand;
import com.smartHome.backend.scene.Device;
import com.smartHome.backend.scene.handler.DeviceCommandHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceCommandDispatcher {

    private final List<DeviceCommandHandler> handlers;

    public DeviceCommandDispatcher(List<DeviceCommandHandler> handlers) {
        this.handlers = handlers;
    }

    public void dispatch(Device device, DeviceCommand command) {
        DeviceCommandHandler handler = handlers.stream()
                .filter(h -> h.supports(device, command))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Nu există handler pentru dispozitivul " + device.getName()
                                + " cu integrarea " + device.getIntegrationType()
                                + " și comanda " + command.getCommandType()
                ));

        handler.execute(device, command);
    }
}