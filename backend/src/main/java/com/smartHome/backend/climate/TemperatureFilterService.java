package com.smartHome.backend.climate;

import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

@Service
public class TemperatureFilterService {

    private static final int WINDOW_SIZE = 5;

    private final Queue<Double> temperatureWindow = new LinkedList<>();

    public synchronized double getFilteredTemperature(double newTemperature) {

        temperatureWindow.add(newTemperature);

        if (temperatureWindow.size() > WINDOW_SIZE) {
            temperatureWindow.poll();
        }

        return temperatureWindow.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(newTemperature);

    }

    public synchronized boolean isReady() {
        return temperatureWindow.size() >= WINDOW_SIZE;
    }

    public synchronized int sampleCount() {
        return temperatureWindow.size();
    }

    public synchronized void reset() {
        temperatureWindow.clear();
    }
}