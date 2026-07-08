package com.smartHome.backend.smartPlug;

import com.smartHome.backend.Config.TapoHomeAssistantProperties;
//import com.smartHome.backend.smartPlug.chartHistory.TapoPlugHistoryService;
import com.smartHome.backend.smartPlug.chartHistory.TapoPlugHistoryRecorder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class TapoPlugService {

    private final WebClient tapoHomeAssistantWebClient;
    private final TapoHomeAssistantProperties properties;
    private final TapoPlugStateStore stateStore;
    private final TapoPlugEventPublisher eventPublisher;
    private final TapoPlugHistoryRecorder historyRecorder;

    public TapoPlugService(
            WebClient tapoHomeAssistantWebClient,
            TapoHomeAssistantProperties properties,
            TapoPlugStateStore stateStore,
            TapoPlugEventPublisher eventPublisher,
            TapoPlugHistoryRecorder historyRecorder
    ) {
        this.tapoHomeAssistantWebClient = tapoHomeAssistantWebClient;
        this.properties = properties;
        this.stateStore = stateStore;
        this.eventPublisher = eventPublisher;
        this.historyRecorder = historyRecorder;
    }

    public TapoPlugEntitiesDto getEntitiesSnapshotFromHomeAssistant() {
        var entities = properties.entities();

        TapoPlugEntitiesDto snapshot = new TapoPlugEntitiesDto(
                getEntityState(entities.switchEntity()),
                getEntityState(entities.powerEntity()),
                getEntityState(entities.energyEntity()),
                getEntityState(entities.voltageEntity()),
                getEntityState(entities.currentEntity())
        );

        stateStore.updateSnapshot(snapshot);
        historyRecorder.recordSnapshotIfAllowed(snapshot);
        eventPublisher.publishSnapshot(snapshot);

        return snapshot;
    }

    public TapoPlugEntitiesDto getLiveEntitiesSnapshot() {
        return stateStore.getSnapshot();
    }

    public void turnOn() {
        callSwitchService("turn_on");
    }

    public void turnOff() {
        callSwitchService("turn_off");
    }

    public void toggle() {
        callSwitchService("toggle");
    }

    private HaEntityStateDto getEntityState(String entityId) {
        return tapoHomeAssistantWebClient.get()
                .uri("/api/states/" + entityId)
                .retrieve()
                .bodyToMono(HaEntityStateDto.class)
                .block();
    }

    private void callSwitchService(String serviceName) {
        String switchEntity = properties.entities().switchEntity();

        tapoHomeAssistantWebClient.post()
                .uri("/api/services/switch/" + serviceName)
                .bodyValue(Map.of("entity_id", switchEntity))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}