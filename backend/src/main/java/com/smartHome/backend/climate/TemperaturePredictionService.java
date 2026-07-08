package com.smartHome.backend.climate;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

@Service
public class TemperaturePredictionService {

    private static final int WINDOW_SIZE = 5;
    private static final int MIN_SAMPLES = 3;

    private static final double MAX_PREDICTION_DELTA_C = 1.0;

    private final Queue<Double> history = new LinkedList<>();

    public synchronized double updateAndPredict(double filteredTemperatureC) {
        history.add(filteredTemperatureC);

        if (history.size() > WINDOW_SIZE) {
            history.poll();
        }

        if (history.size() < MIN_SAMPLES) {
            return filteredTemperatureC;
        }

        List<Double> values = new ArrayList<>(history);

        double totalIncrease = 0.0;

        for (int i = 1; i < values.size(); i++) {
            totalIncrease += values.get(i) - values.get(i - 1);
        }

        double averageIncrease = totalIncrease / (values.size() - 1);

        double rawPrediction = filteredTemperatureC + averageIncrease;

        return clamp(
                rawPrediction,
                filteredTemperatureC - MAX_PREDICTION_DELTA_C,
                filteredTemperatureC + MAX_PREDICTION_DELTA_C
        );

    }

    public synchronized boolean isReady() {
        return history.size() >= WINDOW_SIZE;
    }

    public synchronized int sampleCount() {
        return history.size();
    }

    public synchronized void reset() {
        history.clear();
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}