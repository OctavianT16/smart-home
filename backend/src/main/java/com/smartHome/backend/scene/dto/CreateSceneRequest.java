package com.smartHome.backend.scene.dto;

import java.util.List;

public class CreateSceneRequest {

    private String name;
    private String description;
    private List<SceneActionRequest> actions;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<SceneActionRequest> getActions() {
        return actions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActions(List<SceneActionRequest> actions) {
        this.actions = actions;
    }
}