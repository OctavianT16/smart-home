package com.smartHome.backend.automation;

import com.smartHome.backend.automation.AutomationResponse;
import com.smartHome.backend.automation.AutomationTriggerResponse;
import com.smartHome.backend.automation.CreateAutomationRequest;
import com.smartHome.backend.automation.Automation;
import com.smartHome.backend.scene.Scene;
import com.smartHome.backend.automation.AutomationTriggerType;
import com.smartHome.backend.automation.AutomationRepository;
import com.smartHome.backend.scene.repository.SceneRepository;
import com.smartHome.backend.scene.service.SceneService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class AutomationService {

    private static final ZoneId SYSTEM_ZONE = ZoneId.of("Europe/Bucharest");

    private final AutomationRepository automationRepository;
    private final SceneRepository sceneRepository;
    private final SceneService sceneService;

    public AutomationService(
            AutomationRepository automationRepository,
            SceneRepository sceneRepository,
            SceneService sceneService
    ) {
        this.automationRepository = automationRepository;
        this.sceneRepository = sceneRepository;
        this.sceneService = sceneService;
    }

    @Transactional
    public AutomationResponse createAutomation(CreateAutomationRequest request) {
        if (request.getTriggerType() == null) {
            throw new RuntimeException("Tipul trigger-ului este obligatoriu");
        }

        if (request.getSceneId() == null) {
            throw new RuntimeException("Scenariul este obligatoriu");
        }

        Scene scene = sceneRepository.findById(request.getSceneId())
                .orElseThrow(() -> new RuntimeException("Scenariul nu există"));

        Automation automation = new Automation();
        automation.setName(request.getName());
        automation.setDescription(request.getDescription());
        automation.setTriggerType(request.getTriggerType());
        automation.setEnabled(request.isEnabled());
        automation.setScene(scene);

        if (request.getTriggerType() == AutomationTriggerType.TIME_OF_DAY) {
            if (request.getScheduledTime() == null || request.getScheduledTime().isBlank()) {
                throw new RuntimeException("Ora este obligatorie pentru trigger-ul TIME_OF_DAY");
            }

            automation.setScheduledTime(LocalTime.parse(request.getScheduledTime()));
        } else {
            automation.setScheduledTime(null);
        }

        Automation savedAutomation = automationRepository.save(automation);

        return toAutomationResponse(savedAutomation);
    }

    @Transactional
    public List<AutomationResponse> getAllAutomations() {
        return automationRepository.findAll()
                .stream()
                .map(this::toAutomationResponse)
                .toList();
    }

    @Transactional
    public AutomationResponse getAutomation(Long id) {
        Automation automation = automationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Automatizarea nu există"));

        return toAutomationResponse(automation);
    }

    @Transactional
    public AutomationResponse setEnabled(Long id, boolean enabled) {
        Automation automation = automationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Automatizarea nu există"));

        automation.setEnabled(enabled);

        return toAutomationResponse(automationRepository.save(automation));
    }

    public void deleteAutomation(Long id) {
        if (!automationRepository.existsById(id)) {
            throw new RuntimeException("Automatizarea nu există");
        }

        automationRepository.deleteById(id);
    }

    public AutomationTriggerResponse triggerArriveHome() {
        return executeAutomationsByTrigger(AutomationTriggerType.ARRIVE_HOME);
    }

    public AutomationTriggerResponse triggerLeaveHome() {
        return executeAutomationsByTrigger(AutomationTriggerType.LEAVE_HOME);
    }

    public AutomationTriggerResponse executeAutomationsByTrigger(AutomationTriggerType triggerType) {
        List<Automation> automations = automationRepository
                .findByTriggerTypeAndEnabledTrue(triggerType);

        int executedCount = 0;
        LocalDateTime triggeredAt = LocalDateTime.now(SYSTEM_ZONE);

        for (Automation automation : automations) {
            boolean executed = executeAutomationSafely(automation, triggeredAt);

            if (executed) {
                executedCount++;
            }
        }

        return new AutomationTriggerResponse(
                triggerType,
                automations.size(),
                executedCount
        );
    }

    public void checkTimeBasedAutomations() {
        LocalDateTime now = LocalDateTime.now(SYSTEM_ZONE);
        LocalDate today = now.toLocalDate();

        List<Automation> automations = automationRepository
                .findByTriggerTypeAndEnabledTrue(AutomationTriggerType.TIME_OF_DAY);

        for (Automation automation : automations) {
            if (automation.getScheduledTime() == null) {
                continue;
            }

            boolean sameHour = automation.getScheduledTime().getHour() == now.getHour();
            boolean sameMinute = automation.getScheduledTime().getMinute() == now.getMinute();

            if (!sameHour || !sameMinute) {
                continue;
            }

            if (wasAlreadyTriggeredToday(automation, today)) {
                continue;
            }

            executeAutomationSafely(automation, now);
        }
    }

    private boolean wasAlreadyTriggeredToday(Automation automation, LocalDate today) {
        if (automation.getLastTriggeredAt() == null) {
            return false;
        }

        return automation.getLastTriggeredAt().toLocalDate().equals(today);
    }

    private boolean executeAutomationSafely(Automation automation, LocalDateTime triggeredAt) {
        try {
            automation.setLastTriggeredAt(triggeredAt);
            automationRepository.save(automation);

            sceneService.runScene(automation.getScene().getId());

            return true;
        } catch (Exception e) {
            System.err.println(
                    "Eroare la executarea automatizării "
                            + automation.getId()
                            + " - "
                            + automation.getName()
                            + ": "
                            + e.getMessage()
            );

            return false;
        }
    }

    private AutomationResponse toAutomationResponse(Automation automation) {
        String scheduledTime = automation.getScheduledTime() != null
                ? automation.getScheduledTime().toString()
                : null;

        return new AutomationResponse(
                automation.getId(),
                automation.getName(),
                automation.getDescription(),
                automation.getTriggerType(),
                scheduledTime,
                automation.isEnabled(),
                automation.getCreatedAt(),
                automation.getLastTriggeredAt(),
                automation.getScene().getId(),
                automation.getScene().getName()
        );
    }
}
