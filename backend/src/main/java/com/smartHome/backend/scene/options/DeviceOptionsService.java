package com.smartHome.backend.scene.options;

import com.smartHome.backend.climate.AcDtos;
import com.smartHome.backend.lights.WizSmartBulb.LightMode;
import com.smartHome.backend.dehumidifier.TuyaMode;
import com.smartHome.backend.scene.Device;
import com.smartHome.backend.scene.DeviceCapability;
import com.smartHome.backend.scene.enums.CapabilityType;
import com.smartHome.backend.scene.enums.DeviceCommandType;
import com.smartHome.backend.scene.enums.DeviceType;
import com.smartHome.backend.scene.enums.IntegrationType;
import com.smartHome.backend.scene.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DeviceOptionsService {

    private final DeviceRepository deviceRepository;

    public DeviceOptionsService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional(readOnly = true)
    public DeviceOptionsResponse getCommandOptionsForDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Dispozitivul nu există"));

        List<DeviceCommandOptionResponse> actions = new ArrayList<>();

        if (hasCapability(device, CapabilityType.SWITCH)) {
            actions.add(createSimpleAction(
                    CapabilityType.SWITCH,
                    DeviceCommandType.TURN_ON,
                    "Pornește"
            ));

            actions.add(createSimpleAction(
                    CapabilityType.SWITCH,
                    DeviceCommandType.TURN_OFF,
                    "Oprește"
            ));
        }

        if (device.getIntegrationType() == IntegrationType.WIZ_UDP) {
            addWizOptions(device, actions);
        }

        if (device.getIntegrationType() == IntegrationType.TUYA_CLOUD
                && device.getType() == DeviceType.DEHUMIDIFIER) {
            addTuyaDehumidifierOptions(device, actions);
        }

        if (device.getIntegrationType() == IntegrationType.GREE_UDP
                && device.getType() == DeviceType.AC) {
            addAcOptions(device, actions);
        }


        return new DeviceOptionsResponse(
                device.getId(),
                device.getName(),
                device.getType(),
                device.getIntegrationType(),
                actions
        );
    }

    private void addWizOptions(Device device, List<DeviceCommandOptionResponse> actions) {
        if (hasCapability(device, CapabilityType.PRESET)) {
            List<OptionDto> presets = Arrays.stream(LightMode.values())
                    .map(mode -> new OptionDto(formatLightModeLabel(mode), mode.name()))
                    .toList();

            CommandParameterResponse presetParameter = new CommandParameterResponse(
                    "preset",
                    "Preset WiZ",
                    ParameterInputType.SELECT,
                    null,
                    null,
                    null,
                    presets
            );

            actions.add(new DeviceCommandOptionResponse(
                    CapabilityType.PRESET,
                    DeviceCommandType.SET_PRESET,
                    "Setează preset",
                    List.of(presetParameter)
            ));
        }

        if (hasCapability(device, CapabilityType.BRIGHTNESS)) {
            DeviceCapability brightnessCapability = getCapability(device, CapabilityType.BRIGHTNESS);

            CommandParameterResponse brightnessParameter = new CommandParameterResponse(
                    "brightness",
                    "Luminozitate",
                    ParameterInputType.SLIDER,
                    brightnessCapability.getMinValue(),
                    brightnessCapability.getMaxValue(),
                    brightnessCapability.getUnit(),
                    List.of()
            );



            actions.add(new DeviceCommandOptionResponse(
                    CapabilityType.BRIGHTNESS,
                    DeviceCommandType.SET_BRIGHTNESS,
                    "Setează luminozitatea",
                    List.of(brightnessParameter)
            ));
        }

    }

    private void addTuyaDehumidifierOptions(Device device, List<DeviceCommandOptionResponse> actions) {
        if (hasCapability(device, CapabilityType.MODE)) {
            List<OptionDto> modes = Arrays.stream(TuyaMode.values())
                    .map(mode -> new OptionDto(formatTuyaModeLabel(mode), mode.getValue()))
                    .toList();

            CommandParameterResponse modeParameter = new CommandParameterResponse(
                    "mode",
                    "Mod funcționare",
                    ParameterInputType.SELECT,
                    null,
                    null,
                    null,
                    modes
            );

            actions.add(new DeviceCommandOptionResponse(
                    CapabilityType.MODE,
                    DeviceCommandType.SET_MODE,
                    "Setează modul",
                    List.of(modeParameter)
            ));
        }
    }

    private void addAcOptions(Device device, List<DeviceCommandOptionResponse> actions) {
        if (!hasCapability(device, CapabilityType.CLIMATE)) {
            return;
        }

        CommandParameterResponse powerParameter = new CommandParameterResponse(
                "power",
                "Stare AC",
                ParameterInputType.SELECT,
                null,
                null,
                null,
                List.of(
                        new OptionDto("Pornit", "true"),
                        new OptionDto("Oprit", "false")
                )
        );

        CommandParameterResponse modeParameter = new CommandParameterResponse(
                "mode",
                "Mod funcționare",
                ParameterInputType.SELECT,
                null,
                null,
                null,
                Arrays.stream(AcDtos.Mode.values())
                        .map(mode -> new OptionDto(formatAcModeLabel(mode), mode.name()))
                        .toList()
        );

        CommandParameterResponse temperatureParameter = new CommandParameterResponse(
                "temperatureC",
                "Temperatură",
                ParameterInputType.SLIDER,
                16,
                30,
                "°C",
                List.of()
        );

        CommandParameterResponse fanLevelParameter = new CommandParameterResponse(
                "fanLevel",
                "Nivel ventilator",
                ParameterInputType.SLIDER,
                1,
                5,
                null,
                List.of()
        );

        CommandParameterResponse autoEnabledParameter = new CommandParameterResponse(
                "autoEnabled",
                "Climatizare automată",
                ParameterInputType.SELECT,
                null,
                null,
                null,
                List.of(
                        new OptionDto("Activată", "true"),
                        new OptionDto("Dezactivată", "false")
                )
        );

        actions.add(new DeviceCommandOptionResponse(
                CapabilityType.CLIMATE,
                DeviceCommandType.SET_POWER,
                "Setează pornire/oprire",
                List.of(powerParameter)
        ));

        actions.add(new DeviceCommandOptionResponse(
                CapabilityType.CLIMATE,
                DeviceCommandType.SET_TEMPERATURE,
                "Setează temperatura",
                List.of(temperatureParameter)
        ));

        actions.add(new DeviceCommandOptionResponse(
                CapabilityType.CLIMATE,
                DeviceCommandType.SET_FAN_LEVEL,
                "Setează ventilatorul",
                List.of(fanLevelParameter)
        ));

        actions.add(new DeviceCommandOptionResponse(
                CapabilityType.CLIMATE,
                DeviceCommandType.SET_AUTO_ENABLED,
                "Setează climatizarea automată",
                List.of(autoEnabledParameter)
        ));

        actions.add(new DeviceCommandOptionResponse(
                CapabilityType.CLIMATE,
                DeviceCommandType.SET_AC_STATE,
                "Setează starea completă AC",
                List.of(
                        powerParameter,
                        modeParameter,
                        temperatureParameter,
                        fanLevelParameter,
                        autoEnabledParameter
                )
        ));
    }


    private DeviceCommandOptionResponse createSimpleAction(
            CapabilityType capability,
            DeviceCommandType command,
            String label
    ) {
        return new DeviceCommandOptionResponse(
                capability,
                command,
                label,
                List.of()
        );
    }

    private boolean hasCapability(Device device, CapabilityType capabilityType) {
        return device.getCapabilities()
                .stream()
                .anyMatch(capability -> capability.getCapability() == capabilityType);
    }

    private DeviceCapability getCapability(Device device, CapabilityType capabilityType) {
        return device.getCapabilities()
                .stream()
                .filter(capability -> capability.getCapability() == capabilityType)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Capabilitatea " + capabilityType + " nu există pentru dispozitivul " + device.getName()
                ));
    }

    private String formatLightModeLabel(LightMode mode) {
        return switch (mode) {
            case JUNGLE -> "Jungle";
            case TV_TIME -> "TV Time";
            case RELAX -> "Relax";
            case SUMMER -> "Summer";
        };
    }

    private String formatTuyaModeLabel(TuyaMode mode) {
        return switch (mode) {
            case MANUAL -> "Manual";
            case AUTO -> "Auto";
            case LAUNDRY -> "Laundry";
            case PURIFY -> "Purify";
            case SLEEP -> "Sleep";
        };
    }

    private String formatAcModeLabel(AcDtos.Mode mode) {
        return switch (mode) {
            case COOL -> "Răcire";
            case HEAT -> "Încălzire";
            case DRY -> "Dezumidificare";
            case FAN -> "Ventilație";
            case AUTO -> "Auto";
        };
    }


}