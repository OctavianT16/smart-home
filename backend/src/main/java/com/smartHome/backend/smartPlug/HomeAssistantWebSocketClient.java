package com.smartHome.backend.smartPlug;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartHome.backend.Config.TapoHomeAssistantProperties;
import com.smartHome.backend.smartPlug.chartHistory.TapoPlugHistoryRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class HomeAssistantWebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(HomeAssistantWebSocketClient.class);

    private final TapoHomeAssistantProperties properties;
    private final TapoPlugStateStore stateStore;
    private final TapoPlugEventPublisher eventPublisher;
    private final TapoPlugHistoryRecorder historyRecorder;
    private final ObjectMapper objectMapper;

    private final AtomicInteger messageId = new AtomicInteger(1);
    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private final ConcurrentTaskScheduler taskScheduler = new ConcurrentTaskScheduler();

    public HomeAssistantWebSocketClient(
            TapoHomeAssistantProperties properties,
            TapoPlugStateStore stateStore,
            TapoPlugEventPublisher eventPublisher,
            TapoPlugHistoryRecorder historyRecorder,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.stateStore = stateStore;
        this.eventPublisher = eventPublisher;
        this.historyRecorder = historyRecorder;
        this.objectMapper = objectMapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        if (!connecting.compareAndSet(false, true)) {
            return;
        }

        String wsUrl = buildWebSocketUrl();
        log.info("Connecting to Home Assistant WebSocket: {}", wsUrl);

        StandardWebSocketClient client = new StandardWebSocketClient();

        client.execute(
                new HomeAssistantTextWebSocketHandler(),
                new WebSocketHttpHeaders(),
                URI.create(wsUrl)
        ).whenComplete((session, throwable) -> {
            connecting.set(false);

            if (throwable != null) {
                log.error("Could not connect to Home Assistant WebSocket", throwable);
                scheduleReconnect();
            }
        });
    }

    private String buildWebSocketUrl() {
        String baseUrl = properties.baseUrl();

        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        if (baseUrl.startsWith("https://")) {
            return baseUrl.replaceFirst("https://", "wss://") + "/api/websocket";
        }

        return baseUrl.replaceFirst("http://", "ws://") + "/api/websocket";
    }

    private void scheduleReconnect() {
        taskScheduler.schedule(this::connect, Instant.now().plusSeconds(10));
    }

    private class HomeAssistantTextWebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            log.info("Home Assistant WebSocket connection established");
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            JsonNode root = objectMapper.readTree(message.getPayload());
            String type = root.path("type").asText();

            switch (type) {
                case "auth_required" -> sendAuth(session);
                case "auth_ok" -> {
                    log.info("Authenticated to Home Assistant WebSocket");
                    subscribeToStateChanges(session);
                }
                case "auth_invalid" -> log.error("Invalid Home Assistant WebSocket token");
                case "result" -> handleResult(root);
                case "event" -> handleEvent(root);
                default -> {
                }
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            log.warn("Home Assistant WebSocket connection closed: {}", status);
            scheduleReconnect();
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            log.error("Home Assistant WebSocket transport error", exception);
            scheduleReconnect();
        }
    }

    private void sendAuth(WebSocketSession session) throws IOException {
        sendJson(session, Map.of(
                "type", "auth",
                "access_token", properties.token()
        ));
    }

    private void subscribeToStateChanges(WebSocketSession session) throws IOException {
        int id = messageId.getAndIncrement();

        sendJson(session, Map.of(
                "id", id,
                "type", "subscribe_events",
                "event_type", "state_changed"
        ));

        log.info("Subscribed to Home Assistant state_changed events with id {}", id);
    }

    private void handleResult(JsonNode root) {
        int id = root.path("id").asInt();
        boolean success = root.path("success").asBoolean(false);

        if (success) {
            log.info("Home Assistant WebSocket command {} accepted", id);
        } else {
            log.warn("Home Assistant WebSocket command {} failed: {}", id, root);
        }
    }

    private void handleEvent(JsonNode root) {
        JsonNode event = root.path("event");
        JsonNode data = event.path("data");

        String entityId = data.path("entity_id").asText(null);

        if (entityId == null || !stateStore.isTrackedEntity(entityId)) {
            return;
        }

        JsonNode newState = data.path("new_state");

        if (newState == null || newState.isNull() || newState.isMissingNode()) {
            return;
        }

        HaEntityStateDto entityState = convertNewStateToDto(newState);

        stateStore.updateEntity(entityState);

        TapoPlugEntitiesDto snapshot = stateStore.getSnapshot();
        historyRecorder.recordSnapshotIfAllowed(snapshot);

        eventPublisher.publishEntityUpdate(entityState);

        log.info("Tapo entity updated: {} = {}", entityState.entityId(), entityState.state());
    }

    private HaEntityStateDto convertNewStateToDto(JsonNode newState) {
        Map<String, Object> attributes = objectMapper.convertValue(
                newState.path("attributes"),
                new TypeReference<>() {}
        );

        return new HaEntityStateDto(
                newState.path("entity_id").asText(null),
                newState.path("state").asText(null),
                attributes,
                newState.path("last_changed").asText(null),
                newState.path("last_updated").asText(null)
        );
    }

    private synchronized void sendJson(WebSocketSession session, Map<String, Object> payload) throws IOException {
        String json = objectMapper.writeValueAsString(payload);
        session.sendMessage(new TextMessage(json));
    }
}