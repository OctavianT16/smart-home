package com.smartHome.backend.telemetry.chart;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dht22")
public class Dht22HistoryController {

    private final Dht22HistoryService historyService;

    public Dht22HistoryController(Dht22HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/{deviceId}/history")
    public List<Dht22HistoryPointDto> getHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "60") long minutes
    ) {
        return historyService.getHistory(deviceId, minutes);
    }
}