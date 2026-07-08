package com.smartHome.backend.smartPlug.chartHistory;

import com.smartHome.backend.Config.TapoHomeAssistantProperties;
import com.smartHome.backend.smartPlug.HaEntityStateDto;
import com.smartHome.backend.smartPlug.TapoPlugEntitiesDto;
import org.springframework.stereotype.Service;

@Service
public class TapoPlugHistoryRecorder {

    private final TapoPlugHistoryService tapoHistoryService;

    public TapoPlugHistoryRecorder(
            TapoPlugHistoryService tapoHistoryService,
            TapoHomeAssistantProperties properties
    ) {
        this.tapoHistoryService = tapoHistoryService;
    }

    public void recordSnapshotIfAllowed(TapoPlugEntitiesDto snapshot) {
        if (snapshot == null) {
            return;
        }

        tapoHistoryService.saveSnapshotIfAllowed(
                parseSwitchState(snapshot.switchEntity()),
                parseNumericState(snapshot.powerEntity()),
                parseNumericState(snapshot.energyEntity()),
                parseNumericState(snapshot.voltageEntity()),
                parseNumericState(snapshot.currentEntity())
        );
    }

    private Boolean parseSwitchState(HaEntityStateDto entity) {
        if (entity == null || entity.state() == null) {
            return null;
        }

        String state = entity.state().trim().toLowerCase();

        if (state.equals("on")) {
            return true;
        }

        if (state.equals("off")) {
            return false;
        }

        return null;
    }

    private Double parseNumericState(HaEntityStateDto entity) {
        if (entity == null || entity.state() == null) {
            return null;
        }

        String state = entity.state().trim();

        if (
                state.isBlank() ||
                        state.equalsIgnoreCase("unknown") ||
                        state.equalsIgnoreCase("unavailable")
        ) {
            return null;
        }

        try {
            return Double.parseDouble(state);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}