package com.smartHome.backend.scene.options;

import com.smartHome.backend.scene.options.ParameterInputType;

import java.util.List;

public class CommandParameterResponse {

    private final String name;
    private final String label;
    private final ParameterInputType inputType;

    private final Integer minValue;
    private final Integer maxValue;
    private final String unit;

    private final List<OptionDto> options;

    public CommandParameterResponse(
            String name,
            String label,
            ParameterInputType inputType,
            Integer minValue,
            Integer maxValue,
            String unit,
            List<OptionDto> options
    ) {
        this.name = name;
        this.label = label;
        this.inputType = inputType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
        this.options = options;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public ParameterInputType getInputType() {
        return inputType;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public String getUnit() {
        return unit;
    }

    public List<OptionDto> getOptions() {
        return options;
    }
}