package com.smartHome.backend.dehumidifier;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dehumidifier")
@CrossOrigin
public class DehumidifierController {

    private final TuyaService tuyaService;

    public DehumidifierController(TuyaService tuyaService) {
        this.tuyaService = tuyaService;
    }

    @PostMapping("/on")
    public String turnOn() {
        tuyaService.turnOn();
        return "Dehumidifier ON";
    }

    @PostMapping("/off")
    public String turnOff() {
        tuyaService.turnOff();
        return "Dehumidifier OFF";
    }

    @PostMapping("/mode/{mode}")
    public ResponseEntity<Void> setMode(@PathVariable String mode) {
        tuyaService.setMode(mode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/humidity/{humidity}")
    public ResponseEntity<Void> setTargetHumidity(@PathVariable Integer humidity) {
        tuyaService.setTargetHumidity(humidity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fan-speed/{fanSpeed}")
    public ResponseEntity<Void> setFanSpeed(@PathVariable String fanSpeed) {
        tuyaService.setFanSpeed(fanSpeed);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/countdown/{countdown}")
    public ResponseEntity<Void> setCountdown(@PathVariable String countdown) {
        tuyaService.setCountdown(countdown);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<DehumidifierStatusResponse> getStatus() {
        return ResponseEntity.ok(tuyaService.getDehumidifierStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity
                .badRequest()
                .body(exception.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException exception) {
        return ResponseEntity
                .internalServerError()
                .body(exception.getMessage());
    }
}