package com.smartHome.backend.smartPlug;

import com.smartHome.backend.Config.TapoHomeAssistantProperties;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TapoPlugStateStore {

    private final TapoHomeAssistantProperties properties;
    private final Map<String, HaEntityStateDto> states = new ConcurrentHashMap<>();

    public TapoPlugStateStore(TapoHomeAssistantProperties properties) {
        this.properties = properties;
    }

    public void updateEntity(HaEntityStateDto entityState) {
        if (entityState == null || entityState.entityId() == null) {
            return;
        }

        states.put(entityState.entityId(), entityState);
    }

    public void updateSnapshot(TapoPlugEntitiesDto snapshot) {
        updateEntity(snapshot.switchEntity());
        updateEntity(snapshot.powerEntity());
        updateEntity(snapshot.energyEntity());
        updateEntity(snapshot.voltageEntity());
        updateEntity(snapshot.currentEntity());
    }

    public TapoPlugEntitiesDto getSnapshot() {
        var entities = properties.entities();

        return new TapoPlugEntitiesDto(
                states.get(entities.switchEntity()),
                states.get(entities.powerEntity()),
                states.get(entities.energyEntity()),
                states.get(entities.voltageEntity()),
                states.get(entities.currentEntity())
        );
    }

    public boolean isTrackedEntity(String entityId) {
        return properties.entities().asSet().contains(entityId);
    }
}