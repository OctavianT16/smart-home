package com.smartHome.backend.smartPlug.chartHistory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TapoPlugHistoryService {

    private final TapoPlugMeasurementRepository tapoPlugMeasurementRepository;

    private Instant lastSavedAt;

    @Value("${tapo.history.save-interval-seconds:10}")
    private long saveIntervalSeconds;


    public TapoPlugHistoryService(TapoPlugMeasurementRepository tapoPlugMeasurementRepository) {
        this.tapoPlugMeasurementRepository = tapoPlugMeasurementRepository;
    }

    public TapoPlugMeasurement saveSnapshot(
            Boolean switchOn,
            Double powerW,
            Double energyKwh,
            Double voltageV,
            Double currentA
    ) {
        TapoPlugMeasurement measurement = new TapoPlugMeasurement(
                Instant.now(),
                switchOn,
                powerW,
                energyKwh,
                voltageV,
                currentA
        );

        return tapoPlugMeasurementRepository.save(measurement);
    }

    public TapoPlugMeasurement saveSnapshotIfAllowed(
            Boolean switchOn,
            Double powerW,
            Double energyKwh,
            Double voltageV,
            Double currentA
    ) {
        Instant now = Instant.now();

        if (lastSavedAt != null) {
            long secondsSinceLastSave = Duration.between(lastSavedAt, now).getSeconds();

            if (secondsSinceLastSave < saveIntervalSeconds) {
                return null;
            }
        }

        lastSavedAt = now;

        return saveSnapshot(
                switchOn,
                powerW,
                energyKwh,
                voltageV,
                currentA
        );
    }

}
