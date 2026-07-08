package com.smartHome.backend.automation;

import com.smartHome.backend.automation.AutomationTriggerType;

import java.time.LocalDateTime;

public class AutomationResponse {

    private Long id;
    private String name;
    private String description;
    private AutomationTriggerType triggerType;
    private String scheduledTime;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastTriggeredAt;

    private Long sceneId;
    private String sceneName;

    public AutomationResponse(
            Long id,
            String name,
            String description,
            AutomationTriggerType triggerType,
            String scheduledTime,
            boolean enabled,
            LocalDateTime createdAt,
            LocalDateTime lastTriggeredAt,
            Long sceneId,
            String sceneName
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.triggerType = triggerType;
        this.scheduledTime = scheduledTime;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.lastTriggeredAt = lastTriggeredAt;
        this.sceneId = sceneId;
        this.sceneName = sceneName;
    }

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

    public String getScheduledTime() {
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

    public Long getSceneId() {
        return sceneId;
    }

    public String getSceneName() {
        return sceneName;
    }
}