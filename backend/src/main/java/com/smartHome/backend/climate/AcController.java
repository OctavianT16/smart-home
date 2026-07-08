package com.smartHome.backend.climate;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ac")
public class AcController {

    private final AcService acService;
    private final AcAutomationService acAutomationService;

    public AcController(AcService acService, AcAutomationService acAutomationService) {
        this.acService = acService;
        this.acAutomationService = acAutomationService;
    }

    @PostMapping("/power")
    public ResponseEntity<?> power(@Valid @RequestBody AcDtos.PowerRequest req) throws Exception {
        acService.setPower(req.on(),"ui");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mode")
    public ResponseEntity<?> mode(@Valid @RequestBody AcDtos.ModeRequest req) throws Exception {
        acService.setMode(req.mode(), "ui");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/temperature")
    public ResponseEntity<?> temperature(@Valid @RequestBody AcDtos.TemperatureRequest req) throws Exception {
        acService.setTemperature(req.celsius(), "ui");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fan")
    public ResponseEntity<?> fan(@Valid @RequestBody AcDtos.FanRequest req) throws Exception {
        acService.setFan(req.level(), "ui");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auto")
    public AcState setAutoEnabled(@RequestBody AcDtos.AutoRequest req) {

        acService.setAutoEnabled(req.enabled(), "ui");

        acAutomationService.resetAutomationMemory();

        return acService.getState();
    }

    @PostMapping("/target")
    public ResponseEntity<?> setTargetAmbientTemperature(@RequestBody AcDtos.targetTemperatureRequest req) {

        acService.setTargetAmbientTemperature(req.targetCelsius(), "ui");

        acAutomationService.resetAutomationMemory();

        AcState state = acService.getState();

        return ResponseEntity.ok().body(state);
    }

    @GetMapping("/state")
    public AcState state() {
        return acService.getState();
    }
}
