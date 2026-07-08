package com.smartHome.backend.automation;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutomationScheduler {

    private final AutomationService automationService;

    public AutomationScheduler(AutomationService automationService) {
        this.automationService = automationService;
    }

    @Scheduled(cron = "0 * * * * *", zone = "Europe/Bucharest")
    public void checkTimeBasedAutomations() {
        automationService.checkTimeBasedAutomations();
    }
}