package com.smartHome.backend.scene.handler;

import com.smartHome.backend.climate.AcDtos;
import com.smartHome.backend.climate.AcService;
import com.smartHome.backend.scene.dto.DeviceCommand;
import com.smartHome.backend.scene.Device;
import com.smartHome.backend.scene.enums.DeviceCommandType;
import com.smartHome.backend.scene.enums.DeviceType;
import com.smartHome.backend.scene.enums.IntegrationType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AcCommandHandler implements DeviceCommandHandler {

    private final AcService acService;

    public AcCommandHandler(AcService acService) {
        this.acService = acService;
    }

    @Override
    public boolean supports(Device device, DeviceCommand command) {
        return device.getIntegrationType() == IntegrationType.GREE_UDP
                && device.getType() == DeviceType.AC;
    }

    @Override
    public void execute(Device device, DeviceCommand command) {
        try {
            if (command.getCommandType() == DeviceCommandType.SET_POWER) {
                boolean power = getBoolean(command.getParameters(), "power");
                acService.setPower(power, "scene");
                return;
            }

            if (command.getCommandType() == DeviceCommandType.SET_TEMPERATURE) {
                int temperatureC = getInteger(command.getParameters(), "temperatureC");
                acService.setTemperature(temperatureC, "scene");
                return;
            }

            if (command.getCommandType() == DeviceCommandType.SET_FAN_LEVEL) {
                int fanLevel = getInteger(command.getParameters(), "fanLevel");
                acService.setFan(fanLevel, "scene");
                return;
            }

            if (command.getCommandType() == DeviceCommandType.SET_AUTO_ENABLED) {
                boolean autoEnabled = getBoolean(command.getParameters(), "autoEnabled");
                acService.setAutoEnabled(autoEnabled, "scene");
                return;
            }

            if (command.getCommandType() == DeviceCommandType.SET_AC_STATE) {
                boolean power = getBoolean(command.getParameters(), "power");
                AcDtos.Mode mode = getMode(command.getParameters(), "mode");
                int temperatureC = getInteger(command.getParameters(), "temperatureC");
                int fanLevel = getInteger(command.getParameters(), "fanLevel");
                boolean autoEnabled = getBoolean(command.getParameters(), "autoEnabled");

                acService.applySceneState(
                        power,
                        mode,
                        temperatureC,
                        fanLevel,
                        autoEnabled
                );
                return;
            }

            throw new IllegalArgumentException("Comandă AC necunoscută: " + command.getCommandType());

        } catch (Exception e) {
            throw new RuntimeException("Eroare la executarea comenzii pentru AC", e);
        }
    }

    private int getInteger(Map<String, Object> parameters, String key) {
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

    private boolean getBoolean(Map<String, Object> parameters, String key) {
        if (parameters == null || !parameters.containsKey(key)) {
            throw new IllegalArgumentException("Parametru lipsă: " + key);
        }

        Object value = parameters.get(key);

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (value instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }

        throw new IllegalArgumentException("Parametru invalid pentru " + key + ": " + value);
    }

    private AcDtos.Mode getMode(Map<String, Object> parameters, String key) {
        if (parameters == null || !parameters.containsKey(key)) {
            throw new IllegalArgumentException("Parametru lipsă: " + key);
        }

        Object value = parameters.get(key);

        if (value == null) {
            throw new IllegalArgumentException("Parametru null: " + key);
        }

        return AcDtos.Mode.valueOf(value.toString().toUpperCase());
    }
}