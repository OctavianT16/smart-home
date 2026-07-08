package com.smartHome.backend.automation;


import com.smartHome.backend.automation.AutomationResponse;
import com.smartHome.backend.automation.AutomationTriggerResponse;
import com.smartHome.backend.automation.CreateAutomationRequest;
import com.smartHome.backend.automation.AutomationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/automations")
@CrossOrigin(origins = "http://localhost:5173")
public class AutomationController {

    private final AutomationService automationService;
    @Value("${bearerToken}")
    private String bearerToken;

    public AutomationController(AutomationService automationService) {
        this.automationService = automationService;
    }

    @GetMapping
    public ResponseEntity<List<AutomationResponse>> getAllAutomations() {
        return ResponseEntity.ok(automationService.getAllAutomations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AutomationResponse> getAutomation(@PathVariable Long id) {
        return ResponseEntity.ok(automationService.getAutomation(id));
    }

    @PostMapping
    public ResponseEntity<AutomationResponse> createAutomation(
            @RequestBody CreateAutomationRequest request
    ) {
        return ResponseEntity.ok(automationService.createAutomation(request));
    }

    @PatchMapping("/{id}/enabled")
    public ResponseEntity<AutomationResponse> setEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled
    ) {
        return ResponseEntity.ok(automationService.setEnabled(id, enabled));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAutomation(@PathVariable Long id) {
        automationService.deleteAutomation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/triggers/arrive-home")
    public ResponseEntity<?> triggerArriveHome(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {
        if (authHeader == null ||
                !authHeader.equals("Bearer " + bearerToken)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        return ResponseEntity.ok(automationService.triggerArriveHome());
    }

    @PostMapping("/triggers/leave-home")
    public ResponseEntity<?> triggerLeaveHome(
                @RequestHeader(value = "Authorization", required = false) String authHeader,
                @RequestBody Map<String, String> body) {
            if (authHeader == null ||
                    !authHeader.equals("Bearer " + bearerToken)) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
        return ResponseEntity.ok(automationService.triggerLeaveHome());
    }
}