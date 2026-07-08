package com.smartHome.backend.scene.handler;

import com.smartHome.backend.dehumidifier.TuyaMode;
import com.smartHome.backend.dehumidifier.TuyaService;
import com.smartHome.backend.scene.dto.DeviceCommand;
import com.smartHome.backend.scene.Device;
import com.smartHome.backend.scene.enums.DeviceCommandType;
import com.smartHome.backend.scene.enums.DeviceType;
import com.smartHome.backend.scene.enums.IntegrationType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TuyaDehumidifierCommandHandler implements DeviceCommandHandler {

    private final TuyaService tuyaService;

    public TuyaDehumidifierCommandHandler(TuyaService tuyaService) {
        this.tuyaService = tuyaService;
    }

    @Override
    public boolean supports(Device device, DeviceCommand command) {
        return device.getIntegrationType() == IntegrationType.TUYA_CLOUD
                && device.getType() == DeviceType.DEHUMIDIFIER;
    }

    @Override
    public void execute(Device device, DeviceCommand command) {
        if (command.getCommandType() == DeviceCommandType.TURN_ON) {
            tuyaService.turnOn();
            return;
        }

        if (command.getCommandType() == DeviceCommandType.TURN_OFF) {
            tuyaService.turnOff();
            return;
        }

        if (command.getCommandType() == DeviceCommandType.SET_MODE) {
            String modeValue = getString(command.getParameters(), "mode");
            TuyaMode mode = TuyaMode.fromValue(modeValue);

            tuyaService.setMode(mode);
            return;
        }

        throw new IllegalArgumentException(
                "Comandă Tuya necunoscută pentru dezumidificator: " + command.getCommandType()
        );
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