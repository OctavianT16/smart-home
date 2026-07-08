package com.smartHome.backend.smartPlug.chartHistory;

import java.time.Instant;

public record TapoChartPointDto(Instant timestamp,
                                Double value) {}
