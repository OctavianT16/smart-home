package com.smartHome.backend.scene.handler;

import com.smartHome.backend.lights.WizSmartBulb.LightMode;
import com.smartHome.backend.lights.WizSmartBulb.LightService;
import com.smartHome.backend.scene.dto.DeviceCommand;
import com.smartHome.backend.scene.Device;
import com.smartHome.backend.scene.enums.DeviceCommandType;
import com.smartHome.backend.scene.enums.IntegrationType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WizCommandHandler implements DeviceCommandHandler {

    private final LightService lightService;

    public WizCommandHandler(LightService lightService) {
        this.lightService = lightService;
    }

    @Override
    public boolean supports(Device device, DeviceCommand command) {
        return device.getIntegrationType() == IntegrationType.WIZ_UDP;
    }

    @Override
    public void execute(Device device, DeviceCommand command) {
        String ip = device.getIdentifier();

        if (command.getCommandType() == DeviceCommandType.TURN_ON) {
            lightService.sendLightCommand(ip, true, null, null);
            return;
        }

        if (command.getCommandType() == DeviceCommandType.TURN_OFF) {
            lightService.sendLightCommand(ip, false, null, null);
            return;
        }

        if (command.getCommandType() == DeviceCommandType.SET_BRIGHTNESS) {
            Integer brightness = getInteger(command.getParameters(), "brightness");
            lightService.sendLightCommand(ip, true, brightness, null);
            return;
        }

        if (command.getCommandType() == DeviceCommandType.SET_PRESET) {
            String preset = getString(command.getParameters(), "preset");
            LightMode mode = LightMode.fromString(preset);

            lightService.applyMode(ip, mode);
            return;
        }

        throw new IllegalArgumentException("Comandă WiZ necunoscută: " + command.getCommandType());
    }

    private Integer getInteger(Map<String, Object> parameters, String key) {
        if (parameters == null || !parameters.containsKey(key)) {
            throw new IllegalArgumentException("Parametru lipsă: " + key);
        }

        Object value = parameters.get(key);

        if (value instanceof Integer integerValue) {
            return integerValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        if (value instanceof String stringValue) {
            return Integer.parseInt(stringValue);
        }

        throw new IllegalArgumentException("Parametru invalid pentru " + key + ": " + value);
    }

    private String getString(Map<String, Object> parameters, String key) {
        if (parameters == null || !parameters.containsKey(key)) {
            throw new IllegalArgumentException("Parametru lipsă: " + key);
        }

        Object value = parameters.get(key);

        if (value == null) {
            throw new IllegalArgumentException("Parametru null: " + key);
        }

        return value.toString();
    }
}