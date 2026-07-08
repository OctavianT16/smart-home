package com.smartHome.backend.smartPlug;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TapoPlugEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public TapoPlugEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishEntityUpdate(HaEntityStateDto entityState) {
        messagingTemplate.convertAndSend("/topic/tapo-plug/entity", entityState);
    }

    public void publishSnapshot(TapoPlugEntitiesDto snapshot) {
        messagingTemplate.convertAndSend("/topic/tapo-plug/entities", snapshot);
    }
}