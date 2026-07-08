package com.smartHome.backend.automation;

import com.smartHome.backend.automation.AutomationTriggerType;

public class CreateAutomationRequest {

    private String name;
    private String description;
    private AutomationTriggerType triggerType;
    private String scheduledTime;
    private boolean enabled = true;
    private Long sceneId;

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

    public Long getSceneId() {
        return sceneId;
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

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
    }
}