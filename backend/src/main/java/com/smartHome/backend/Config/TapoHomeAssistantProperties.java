package com.smartHome.backend.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "tapo.home-assistant")
public record TapoHomeAssistantProperties(
        String baseUrl,
        String token,
        Entities entities
) {
    public record Entities(
            String switchEntity,
            String powerEntity,
            String energyEntity,
            String voltageEntity,
            String currentEntity
    ) {
        public Set<String> asSet() {
            return Set.of(
                    switchEntity,
                    powerEntity,
                    energyEntity,
                    voltageEntity,
                    currentEntity
            );
        }
    }
}