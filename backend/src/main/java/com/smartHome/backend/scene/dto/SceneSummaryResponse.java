package com.smartHome.backend.scene.dto;

import java.time.LocalDateTime;

public class SceneSummaryResponse {

    private Long id;
    private String name;
    private String description;
    private boolean enabled;
    private LocalDateTime createdAt;
    private int actionCount;
    private int deviceCount;

    public SceneSummaryResponse(
            Long id,
            String name,
            String description,
            boolean enabled,
            LocalDateTime createdAt,
            int actionCount,
            int deviceCount
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.actionCount = actionCount;
        this.deviceCount = deviceCount;
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

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getActionCount() {
        return actionCount;
    }

    public int getDeviceCount() {
        return deviceCount;
    }
}