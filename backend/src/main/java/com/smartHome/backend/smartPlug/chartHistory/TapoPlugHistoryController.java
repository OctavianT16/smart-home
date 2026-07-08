package com.smartHome.backend.smartPlug.chartHistory;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tapo/history")
@CrossOrigin(origins = "*")
public class TapoPlugHistoryController {
    private final TapoPlugHistoryQueryService historyQueryService;

    public TapoPlugHistoryController(TapoPlugHistoryQueryService historyQueryService) {
        this.historyQueryService = historyQueryService;
    }

    @GetMapping("/chart")
    public List<TapoChartPointDto> getChartData(
            @RequestParam(defaultValue = "POWER") TapoChartMetric metric,
            @RequestParam(defaultValue = "LAST_HOUR") TapoChartPeriod period
    ) {
        return historyQueryService.getChartData(metric, period);
    }
}
