package com.smartHome.backend.scene.handler;

import com.smartHome.backend.scene.dto.DeviceCommand;
import com.smartHome.backend.scene.Device;
import com.smartHome.backend.scene.enums.DeviceCommandType;
import com.smartHome.backend.scene.enums.DeviceType;
import com.smartHome.backend.scene.enums.IntegrationType;
import com.smartHome.backend.smartPlug.TapoPlugService;
import org.springframework.stereotype.Component;

@Component
public class TapoPlugCommandHandler implements DeviceCommandHandler {

    private final TapoPlugService tapoPlugService;

    public TapoPlugCommandHandler(TapoPlugService tapoPlugService) {
        this.tapoPlugService = tapoPlugService;
    }

    @Override
    public boolean supports(Device device, DeviceCommand command) {
        return device.getIntegrationType() == IntegrationType.TAPO
                && device.getType() == DeviceType.SMART_PLUG;
    }

    @Override
    public void execute(Device device, DeviceCommand command) {
        if (command.getCommandType() == DeviceCommandType.TURN_ON) {
            tapoPlugService.turnOn();
            return;
        }

        if (command.getCommandType() == DeviceCommandType.TURN_OFF) {
            tapoPlugService.turnOff();
            return;
        }

        throw new IllegalArgumentException("Comandă Tapo necunoscută: " + command.getCommandType());
    }
}