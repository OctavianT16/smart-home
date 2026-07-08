package com.smartHome.backend.climate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class AcAutomationService {

    private final AcService acService;
    private final TemperatureFilterService movingAverageService;
    private final TemperaturePredictionService predictionService;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "ac-automation-control-loop"));

    private volatile ClimateReading latestReading;

    private volatile Instant lastAutomationOnAt = Instant.EPOCH;
    private volatile Instant lastAutomationOffAt = Instant.EPOCH;

    private static final Duration CONTROL_LOOP_INTERVAL = Duration.ofSeconds(15);
    private static final Duration MAX_READING_AGE = Duration.ofMinutes(2);


    private static final double HYSTERESIS_DELTA_C = 1.5;

    private static final double PREDICTION_START_MARGIN_C = 0.5;

    private static final int AC_SETPOINT_OFFSET_C = 2;

    private static final int MIN_AC_TEMPERATURE_C = 16;
    private static final int MAX_AC_TEMPERATURE_C = 30;
    private static final Duration MIN_ON_TIME = Duration.ofMinutes(5);
    private static final Duration MIN_OFF_TIME = Duration.ofMinutes(1);

    public AcAutomationService(
            AcService acService,
            TemperatureFilterService movingAverageService,
            TemperaturePredictionService predictionService
    ) {
        this.acService = acService;
        this.movingAverageService = movingAverageService;
        this.predictionService = predictionService;
    }

    @PostConstruct
    public void startControlLoop() {
        scheduler.scheduleWithFixedDelay(
                this::safeControlLoop,
                CONTROL_LOOP_INTERVAL.toSeconds(),
                CONTROL_LOOP_INTERVAL.toSeconds(),
                TimeUnit.SECONDS
        );

        System.out.println("[AC Automation] Control loop started. Interval: "
                + CONTROL_LOOP_INTERVAL.toSeconds() + " seconds.");
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
        System.out.println("[AC Automation] Control loop stopped.");
    }

    public void onTemperature(double rawTemperatureC) {
        AcState state = acService.getState();

        if (!state.autoEnabled()) {
            System.out.printf(
                    Locale.US,
                    "[AC Automation] Ignored temperature %.2f°C because automation is disabled.%n",
                    rawTemperatureC
            );
            return;
        }

        Instant now = Instant.now();

        double filteredTemperatureC = movingAverageService.getFilteredTemperature(rawTemperatureC);

        double predictedTemperatureC = predictionService.updateAndPredict(filteredTemperatureC);

        latestReading = new ClimateReading(
                rawTemperatureC,
                filteredTemperatureC,
                predictedTemperatureC,
                now
        );

        System.out.printf(
                Locale.US,
                "[AC Automation] Reading updated | Raw=%.2f°C | Filtered=%.2f°C | Predicted=%.2f°C%n",
                rawTemperatureC,
                filteredTemperatureC,
                predictedTemperatureC
        );
    }

    private void safeControlLoop() {
        try {
            controlLoop();
        } catch (Exception e) {
            System.err.println("[AC Automation] Error in control loop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void controlLoop() throws Exception {
        AcState state = acService.getState();

        if (!state.autoEnabled()) {
            return;
        }

        ClimateReading reading = latestReading;

        if (reading == null) {
            System.out.println("[AC Automation] No temperature reading available yet.");
            return;
        }

        Instant now = Instant.now();

        if (reading.timestamp().isBefore(now.minus(MAX_READING_AGE))) {
            System.out.println("[AC Automation] Latest reading is too old. Skipping decision.");
            return;
        }

        int targetAmbientC = state.targetAmbientTemperatureC();

        double upperThreshold = targetAmbientC + HYSTERESIS_DELTA_C;
        double lowerThreshold = targetAmbientC - HYSTERESIS_DELTA_C;

        int acSetpointC = calculateAcSetpoint(targetAmbientC);

        boolean crossedUpperThreshold =
                reading.filteredTemperatureC() >= upperThreshold;

        boolean predictedCrossingSoon =
                predictionService.isReady()
                        && reading.filteredTemperatureC() >= upperThreshold - PREDICTION_START_MARGIN_C
                        && reading.predictedTemperatureC() >= upperThreshold;

        boolean shouldStartCooling =
                crossedUpperThreshold || predictedCrossingSoon;

        boolean shouldStopCooling =
                reading.filteredTemperatureC() <= lowerThreshold;

        System.out.printf(
                Locale.US,
                """
                [AC Automation] Control decision
                  AutoEnabled: %s
                  AC Power: %s
                  AC Mode: %s
                  Target ambient: %d°C
                  AC setpoint if started: %d°C
                  Raw: %.2f°C
                  Filtered: %.2f°C
                  Predicted: %.2f°C
                  Upper threshold: %.2f°C
                  Lower threshold: %.2f°C
                  Crossed upper: %s
                  Predicted crossing soon: %s
                  Should start cooling: %s
                  Should stop cooling: %s
                %n""",
                state.autoEnabled(),
                state.power(),
                state.mode(),
                targetAmbientC,
                acSetpointC,
                reading.rawTemperatureC(),
                reading.filteredTemperatureC(),
                reading.predictedTemperatureC(),
                upperThreshold,
                lowerThreshold,
                crossedUpperThreshold,
                predictedCrossingSoon,
                shouldStartCooling,
                shouldStopCooling
        );

        if (!state.power()) {
            handleAcOffState(now, shouldStartCooling, acSetpointC);
            return;
        }

        if (state.power() && state.mode() == AcDtos.Mode.COOL) {
            handleAcCoolingState(now, shouldStopCooling);
        }
    }

    private void handleAcOffState(
            Instant now,
            boolean shouldStartCooling,
            int acSetpointC
    ) throws Exception {
        if (!shouldStartCooling) {
            return;
        }

        Instant allowedStartTime = lastAutomationOffAt.plus(MIN_OFF_TIME);

        if (now.isBefore(allowedStartTime)) {
            return;
        }

        startCooling(acSetpointC, now);
    }

    private void handleAcCoolingState(
            Instant now,
            boolean shouldStopCooling
    ) throws Exception {
        if (!shouldStopCooling) {
            return;
        }

        Instant allowedStopTime = lastAutomationOnAt.plus(MIN_ON_TIME);

        if (now.isBefore(allowedStopTime)) {
            return;
        }

        stopCooling(now);
    }

    private void startCooling(int acSetpointC, Instant now) throws Exception {

        acService.setCoolingMode(true, AcDtos.Mode.COOL, 3, acSetpointC, "automation");

        lastAutomationOnAt = now;

    }

    private void stopCooling(Instant now) throws Exception {

        acService.setPower(false, "automation");

        lastAutomationOffAt = now;

    }

    private int calculateAcSetpoint(int targetAmbientC) {
        int acSetpoint = targetAmbientC - AC_SETPOINT_OFFSET_C;

        return Math.max(
                MIN_AC_TEMPERATURE_C,
                Math.min(MAX_AC_TEMPERATURE_C, acSetpoint)
        );
    }

    public void resetAutomationMemory() {
        movingAverageService.reset();
        predictionService.reset();
        latestReading = null;
    }

    private record ClimateReading(
            double rawTemperatureC,
            double filteredTemperatureC,
            double predictedTemperatureC,
            Instant timestamp
    ) {
    }
}