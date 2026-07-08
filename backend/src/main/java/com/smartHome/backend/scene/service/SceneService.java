package com.smartHome.backend.scene.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartHome.backend.scene.dto.*;
import com.smartHome.backend.scene.Device;
import com.smartHome.backend.scene.Scene;
import com.smartHome.backend.scene.SceneAction;
import com.smartHome.backend.scene.repository.DeviceRepository;
import com.smartHome.backend.scene.repository.SceneActionRepository;
import com.smartHome.backend.scene.repository.SceneRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SceneService {

    private final SceneRepository sceneRepository;
    private final SceneActionRepository sceneActionRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceCommandDispatcher commandDispatcher;
    private final ObjectMapper objectMapper;

    public SceneService(
            SceneRepository sceneRepository,
            SceneActionRepository sceneActionRepository,
            DeviceRepository deviceRepository,
            DeviceCommandDispatcher commandDispatcher,
            ObjectMapper objectMapper
    ) {
        this.sceneRepository = sceneRepository;
        this.sceneActionRepository = sceneActionRepository;
        this.deviceRepository = deviceRepository;
        this.commandDispatcher = commandDispatcher;
        this.objectMapper = objectMapper;
    }



    public List<SceneResponse> getAllScenes() {
        return sceneRepository.findAll()
                .stream()
                .map(this::toSceneResponse)
                .toList();
    }

    public SceneResponse getScene(Long id) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scenariul nu există"));

        return toSceneResponse(scene);
    }

    public List<SceneSummaryResponse> getSceneSummaries() {
        return sceneRepository.findAll()
                .stream()
                .map(this::toSceneSummaryResponse)
                .toList();
    }

    @Transactional
    public SceneResponse createScene(CreateSceneRequest request) {
        Scene scene = new Scene();
        scene.setName(request.getName());
        scene.setDescription(request.getDescription());
        scene.setEnabled(true);

        Scene savedScene = sceneRepository.save(scene);

        int order = 1;

        for (SceneActionRequest actionRequest : request.getActions()) {
            Device device = deviceRepository.findById(actionRequest.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Dispozitivul nu există"));

            SceneAction action = new SceneAction();
            action.setScene(savedScene);
            action.setDevice(device);
            action.setCapability(actionRequest.getCapability());
            action.setCommandType(actionRequest.getCommandType());

            if (actionRequest.getExecutionOrder() != null) {
                action.setExecutionOrder(actionRequest.getExecutionOrder());
            } else {
                action.setExecutionOrder(order);
            }

            if (actionRequest.getDelayMs() != null) {
                action.setDelayMs(actionRequest.getDelayMs());
            } else {
                action.setDelayMs(0);
            }

            try {
                Map<String, Object> parameters = actionRequest.getParameters() != null
                        ? actionRequest.getParameters()
                        : new HashMap<>();

                String parametersJson = objectMapper.writeValueAsString(parameters);
                action.setParametersJson(parametersJson);
            } catch (Exception e) {
                throw new RuntimeException("Parametrii acțiunii nu au putut fi salvați", e);
            }

            sceneActionRepository.save(action);
            order++;
        }

        return getScene(savedScene.getId());
    }

    public void runScene(Long sceneId) {
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new RuntimeException("Scenariul nu există"));

        if (!scene.isEnabled()) {
            throw new RuntimeException("Scenariul este dezactivat");
        }

        List<SceneAction> actions = sceneActionRepository
                .findBySceneIdOrderByExecutionOrderAsc(sceneId);

        for (SceneAction action : actions) {
            executeAction(action);

            if (action.getDelayMs() != null && action.getDelayMs() > 0) {
                sleep(action.getDelayMs());
            }
        }
    }

    private void executeAction(SceneAction action) {
        Device device = action.getDevice();

        if (device == null) {
            throw new RuntimeException("Acțiunea nu are dispozitiv asociat");
        }

        if (!device.isEnabled()) {
            throw new RuntimeException("Dispozitivul este dezactivat: " + device.getName());
        }

        try {
            Map<String, Object> parameters = readParameters(action.getParametersJson());

            DeviceCommand command = new DeviceCommand();
            command.setCapability(action.getCapability());
            command.setCommand(action.getCommandType());
            command.setParameters(parameters);

            commandDispatcher.dispatch(device, command);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Eroare la executarea acțiunii pentru dispozitivul: " + device.getName(),
                    e
            );
        }
    }

    private Map<String, Object> readParameters(String parametersJson) {
        try {
            if (parametersJson == null || parametersJson.isBlank()) {
                return new HashMap<>();
            }

            return objectMapper.readValue(
                    parametersJson,
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Parametrii acțiunii nu au putut fi interpretați", e);
        }
    }

    private void sleep(Integer delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Execuția scenariului a fost întreruptă", e);
        }
    }

    public void deleteScene(Long id) {
        if (!sceneRepository.existsById(id)) {
            throw new RuntimeException("Scenariul nu există");
        }

        sceneRepository.deleteById(id);
    }

    private SceneResponse toSceneResponse(Scene scene) {
        List<SceneAction> actions = sceneActionRepository
                .findBySceneIdOrderByExecutionOrderAsc(scene.getId());

        List<SceneActionResponse> actionResponses = actions.stream()
                .map(this::toSceneActionResponse)
                .toList();

        return new SceneResponse(
                scene.getId(),
                scene.getName(),
                scene.getDescription(),
                scene.isEnabled(),
                scene.getCreatedAt(),
                actionResponses
        );
    }

    private SceneActionResponse toSceneActionResponse(SceneAction action) {
        Map<String, Object> parameters = readParameters(action.getParametersJson());

        return new SceneActionResponse(
                action.getId(),
                action.getExecutionOrder(),
                action.getDelayMs(),
                action.getDevice().getId(),
                action.getDevice().getName(),
                action.getCapability(),
                action.getCommandType(),
                parameters
        );
    }

    private SceneSummaryResponse toSceneSummaryResponse(Scene scene) {
        List<SceneAction> actions = sceneActionRepository
                .findBySceneIdOrderByExecutionOrderAsc(scene.getId());

        int actionCount = actions.size();

        int deviceCount = (int) actions.stream()
                .map(action -> action.getDevice().getId())
                .distinct()
                .count();

        return new SceneSummaryResponse(
                scene.getId(),
                scene.getName(),
                scene.getDescription(),
                scene.isEnabled(),
                scene.getCreatedAt(),
                actionCount,
                deviceCount
        );
    }
}