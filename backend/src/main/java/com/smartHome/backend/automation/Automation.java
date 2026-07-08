package com.smartHome.backend.automation;

import com.smartHome.backend.automation.AutomationTriggerType;
import com.smartHome.backend.scene.Scene;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "automations")
public class Automation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private AutomationTriggerType triggerType;

    private LocalTime scheduledTime;

    private boolean enabled = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastTriggeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id")
    private Scene scene;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public AutomationTriggerType getTriggerType() {
        return triggerType;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public Scene getScene() {
        return scene;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTriggerType(AutomationTriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
