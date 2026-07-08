package com.smartHome.backend.telemetry.chart;

import com.smartHome.backend.telemetry.Dht22TelemetryDto;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class Dht22HistoryService {

    private final Dht22MeasurementRepository repository;

    private static final long SAVE_INTERVAL_MS = 10_000;

    private final Map<String, Long> lastSavedAtByDevice = new ConcurrentHashMap<>();

    public Dht22HistoryService(Dht22MeasurementRepository repository) {
        this.repository = repository;
    }

    public void saveIfAllowed(Dht22TelemetryDto dto) {
        if (dto.temperatureC == null || dto.humidityPct == null) {
            return;
        }

        String deviceId = dto.deviceId != null ? dto.deviceId : "unknown";
        String sensor = dto.sensor != null ? dto.sensor : "dht22";

        long nowMs = System.currentTimeMillis();

        Long lastSavedAt = lastSavedAtByDevice.get(deviceId);

        if (lastSavedAt != null && nowMs - lastSavedAt < SAVE_INTERVAL_MS) {
            return;
        }

        Dht22Measurement measurement = new Dht22Measurement(
                deviceId,
                sensor,
                dto.temperatureC,
                dto.humidityPct,
                dto.ts,
                Instant.now()
        );

        repository.save(measurement);
        lastSavedAtByDevice.put(deviceId, nowMs);

    }

    public List<Dht22HistoryPointDto> getHistory(String deviceId, long minutes) {
        Instant end = Instant.now();
        Instant start = end.minus(Duration.ofMinutes(minutes));

        List<Dht22Measurement> measurements = repository.findByDeviceIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                deviceId,start,end);

        if (minutes >= 1440)
        {
            return aggregateChart(measurements, Duration.ofMinutes(5));
        }

        return measurements.stream()
                .map(measurement -> new Dht22HistoryPointDto(
                        measurement.getRecordedAt().toEpochMilli(),
                        measurement.getTemperatureC(),
                        measurement.getHumidityPct()
                ))
                .toList();
    }

    private List<Dht22HistoryPointDto> aggregateChart(List<Dht22Measurement> measurements, Duration bucketDuration) {

        long bucketMs = bucketDuration.toMillis();

        Map<Long, List<Dht22Measurement>> groupedByBucket = measurements.stream()
                .filter(m -> m.getRecordedAt() != null)
                .filter(m -> m.getTemperatureC() != null && m.getHumidityPct() != null)
                .collect(Collectors.groupingBy(
                        measurement -> {
                            long timestampMs = measurement.getRecordedAt().toEpochMilli();
                            return (timestampMs / bucketMs) * bucketMs;
                        },
                        TreeMap::new,
                        Collectors.toList()
                ));

        return groupedByBucket.entrySet()
                .stream()
                .map(entry -> {
                    Long bucketTimestamp = entry.getKey();
                    List<Dht22Measurement> bucketMeasurements = entry.getValue();

                    double avgTemperature = bucketMeasurements.stream()
                            .mapToDouble(Dht22Measurement::getTemperatureC)
                            .average()
                            .orElse(0.0);

                    double avgHumidity = bucketMeasurements.stream()
                            .mapToDouble(Dht22Measurement::getHumidityPct)
                            .average()
                            .orElse(0.0);

                    return new Dht22HistoryPointDto(
                            bucketTimestamp,
                            avgTemperature,
                            avgHumidity
                    );
                })
                .sorted(Comparator.comparing(Dht22HistoryPointDto::ts))
                .toList();

    }
}