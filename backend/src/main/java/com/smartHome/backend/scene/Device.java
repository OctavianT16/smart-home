package com.smartHome.backend.scene;

import com.smartHome.backend.scene.enums.DeviceType;
import com.smartHome.backend.scene.enums.IntegrationType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String room;

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    @Enumerated(EnumType.STRING)
    private IntegrationType integrationType;

    private String identifier;

    private boolean enabled = true;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeviceCapability> capabilities = new ArrayList<>();

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

    public List<DeviceCapability> getCapabilities() {
        return capabilities;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public void setIntegrationType(IntegrationType integrationType) {
        this.integrationType = integrationType;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCapabilities(List<DeviceCapability> capabilities) {
        this.capabilities = capabilities;
    }
}