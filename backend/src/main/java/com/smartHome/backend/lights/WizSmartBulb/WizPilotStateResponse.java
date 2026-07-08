package com.smartHome.backend.lights.WizSmartBulb;

public record WizPilotStateResponse(
        boolean state,
        Integer temp,
        Integer dimming,
        String sceneId) {
}
