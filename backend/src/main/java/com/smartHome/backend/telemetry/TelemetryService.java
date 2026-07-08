package com.smartHome.backend.telemetry;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartHome.backend.telemetry.chart.Dht22HistoryService;
import com.smartHome.backend.climate.AcAutomationService;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TelemetryService {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final AcAutomationService acAutomationService;
    private final Dht22HistoryService dht22HistoryService;

    public TelemetryService(ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate, AcAutomationService acAutomationService, Dht22HistoryService dht22HistoryService) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.acAutomationService = acAutomationService;
        this.dht22HistoryService = dht22HistoryService;
    }
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMqttMessage(Message<?> message) {
        String payload = String.valueOf(message.getPayload());

        try {
            Dht22TelemetryDto dto = objectMapper.readValue(payload, Dht22TelemetryDto.class);

            System.out.println("[MQTT] Dto: " + dto);

            String deviceId = dto.deviceId != null ? dto.deviceId : "unknown";
            String wsTopic = "/topic/telemetry/dht22/" + deviceId;

            dht22HistoryService.saveIfAllowed(dto);
            acAutomationService.onTemperature(dto.temperatureC);
            messagingTemplate.convertAndSend(wsTopic, dto);

        } catch (Exception ex) {
            System.out.println("[MQTT] Eroare la parsare: " + ex.getMessage());
        }
    }
}
