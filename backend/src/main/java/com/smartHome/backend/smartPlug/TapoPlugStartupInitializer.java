package com.smartHome.backend.smartPlug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class TapoPlugStartupInitializer {

    private static final Logger log = LoggerFactory.getLogger(TapoPlugStartupInitializer.class);

    private final TapoPlugService tapoPlugService;

    public TapoPlugStartupInitializer(TapoPlugService tapoPlugService) {
        this.tapoPlugService = tapoPlugService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeTapoPlugState() {
        try {
            tapoPlugService.getEntitiesSnapshotFromHomeAssistant();
            log.info("Initial Tapo plug snapshot loaded from Home Assistant");
        } catch (Exception e) {
            log.error("Could not load initial Tapo plug snapshot", e);
        }
    }
}