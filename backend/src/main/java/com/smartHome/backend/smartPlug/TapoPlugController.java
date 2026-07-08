package com.smartHome.backend.smartPlug;

import com.smartHome.backend.smartPlug.chartHistory.TapoPlugMeasurementRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tapo-plug")
@CrossOrigin(origins = "*")
public class TapoPlugController {

    private final TapoPlugService tapoPlugService;
    private final TapoPlugMeasurementRepository tapoPlugMeasurementRepository;

    public TapoPlugController(TapoPlugService tapoPlugService, TapoPlugMeasurementRepository tapoPlugMeasurementRepository) {
        this.tapoPlugService = tapoPlugService;
        this.tapoPlugMeasurementRepository = tapoPlugMeasurementRepository;
    }

    @GetMapping("/entities")
    public TapoPlugEntitiesDto getEntitiesFromHomeAssistant() {
        return tapoPlugService.getEntitiesSnapshotFromHomeAssistant();
    }

    @GetMapping("/live/entities")
    public TapoPlugEntitiesDto getLiveEntities() {
        return tapoPlugService.getLiveEntitiesSnapshot();
    }

    @PostMapping("/on")
    public String turnOn() {
        tapoPlugService.turnOn();
        return "Tapo plug turn_on command sent";
    }

    @PostMapping("/off")
    public String turnOff() {
        tapoPlugService.turnOff();
        return "Tapo plug turn_off command sent";
    }

    @PostMapping("/toggle")
    public String toggle() {
        tapoPlugService.toggle();
        return "Tapo plug toggle command sent";
    }

}