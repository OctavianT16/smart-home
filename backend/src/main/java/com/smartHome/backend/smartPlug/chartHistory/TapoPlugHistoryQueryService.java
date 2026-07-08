package com.smartHome.backend.smartPlug.chartHistory;

import org.springframework.stereotype.Service;
import java.util.List;
import java.time.Instant;


@Service
public class TapoPlugHistoryQueryService {
    private final TapoPlugMeasurementRepository repository;

    public TapoPlugHistoryQueryService(TapoPlugMeasurementRepository repository) {
        this.repository = repository;
    }

    public List<TapoChartPointDto> getChartData(TapoChartMetric metric, TapoChartPeriod period){
        Instant end = Instant.now();
        Instant start = end.minus(period.getDuration());

        List<TapoPlugMeasurement> measurements = repository.findByRecordedAtBetweenOrderByRecordedAtAsc(start, end);

        return measurements.stream()
                .map(measurement -> toChartPoint(measurement, metric))
                .filter(point -> point.value() != null)
                .toList();
    }

    private TapoChartPointDto toChartPoint(TapoPlugMeasurement measurement, TapoChartMetric metric) {
        Double value = switch (metric) {
            case POWER -> measurement.getPowerW();
            case ENERGY -> measurement.getEnergyKwh();
            case VOLTAGE -> measurement.getVoltageV();
            case CURRENT -> measurement.getCurrentA();
        };

        return new TapoChartPointDto(measurement.getRecordedAt(), value);
    }
}
