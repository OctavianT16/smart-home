package com.smartHome.backend.scene;

import com.smartHome.backend.scene.enums.CapabilityType;
import com.smartHome.backend.scene.enums.DeviceCommandType;
import jakarta.persistence.*;

@Entity
@Table(name = "scene_actions")
public class SceneAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer executionOrder;

    private Integer delayMs = 0;

    @Enumerated(EnumType.STRING)
    private CapabilityType capability;

    @Enumerated(EnumType.STRING)
    @Column(name = "command_type", length = 50, nullable = false)
    private DeviceCommandType commandType;

    @Column(columnDefinition = "TEXT")
    private String parametersJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id")
    private Scene scene;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id")
    private Device device;

    public Long getId() {
        return id;
    }

    public Integer getExecutionOrder() {
        return executionOrder;
    }

    public Integer getDelayMs() {
        return delayMs;
    }

    public CapabilityType getCapability() {
        return capability;
    }

    public DeviceCommandType getCommandType() {
        return commandType;
    }

    public String getParametersJson() {
        return parametersJson;
    }

    public Scene getScene() {
        return scene;
    }

    public Device getDevice() {
        return device;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setExecutionOrder(Integer executionOrder) {
        this.executionOrder = executionOrder;
    }

    public void setDelayMs(Integer delayMs) {
        this.delayMs = delayMs;
    }

    public void setCapability(CapabilityType capability) {
        this.capability = capability;
    }

    public void setCommandType(DeviceCommandType command) {
        this.commandType = command;
    }

    public void setParametersJson(String parametersJson) {
        this.parametersJson = parametersJson;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}