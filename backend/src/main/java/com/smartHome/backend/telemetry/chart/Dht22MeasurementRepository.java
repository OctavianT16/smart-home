package com.smartHome.backend.telemetry.chart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface Dht22MeasurementRepository extends JpaRepository<Dht22Measurement, Long> {

    List<Dht22Measurement> findByDeviceIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            String deviceId,
            Instant start,
            Instant end
    );
}