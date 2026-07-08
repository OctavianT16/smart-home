package com.smartHome.backend.smartPlug;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record HaEntityStateDto(
        @JsonProperty("entity_id")
        String entityId,

        String state,

        Map<String, Object> attributes,

        @JsonProperty("last_changed")
        String lastChanged,

        @JsonProperty("last_updated")
        String lastUpdated
) {
}