package com.smartHome.backend.scene.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SceneResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final boolean enabled;
    private final LocalDateTime createdAt;
    private final List<SceneActionResponse> actions;

    public SceneResponse(
            Long id,
            String name,
            String description,
            boolean enabled,
            LocalDateTime createdAt,
            List<SceneActionResponse> actions
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.actions = actions;
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

    public List<SceneActionResponse> getActions() {
        return actions;
    }
}