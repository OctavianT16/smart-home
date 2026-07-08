package com.smartHome.backend.scene.dto;

import com.smartHome.backend.scene.enums.DeviceType;
import com.smartHome.backend.scene.enums.IntegrationType;

import java.util.List;

public class DeviceResponse {

    private final Long id;
    private final String name;
    private final String room;
    private final DeviceType type;
    private final IntegrationType integrationType;
    private final String identifier;
    private final boolean enabled;
    private final List<DeviceCapabilityResponse> capabilities;

    public DeviceResponse(
            Long id,
            String name,
            String room,
            DeviceType type,
            IntegrationType integrationType,
            String identifier,
            boolean enabled,
            List<DeviceCapabilityResponse> capabilities
    ) {
        this.id = id;
        this.name = name;
        this.room = room;
        this.type = type;
        this.integrationType = integrationType;
        this.identifier = identifier;
        this.enabled = enabled;
        this.capabilities = capabilities;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRoom() {
        return room;
    }

    public DeviceType getType() {
        return type;
    }

    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<DeviceCapabilityResponse> getCapabilities() {
        return capabilities;
    }
}