package com.smartHome.backend.smartPlug.chartHistory;

import jakarta.persistence.*;
import java.time.Instant;


@Entity
@Table(name = "tapo_measurements")

public class TapoPlugMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant recordedAt;

    private Boolean switchOn;

    private Double powerW;

    private Double energyKwh;

    private Double voltageV;

    private Double currentA;

    public TapoPlugMeasurement() {
    }

    public TapoPlugMeasurement(
            Instant recordedAt,
            Boolean switchOn,
            Double powerW,
            Double energyKwh,
            Double voltageV,
            Double currentA
    ) {
        this.recordedAt = recordedAt;
        this.switchOn = switchOn;
        this.powerW = powerW;
        this.energyKwh = energyKwh;
        this.voltageV = voltageV;
        this.currentA = currentA;
    }

    public Long getId() {
        return id;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Boolean getSwitchOn() {
        return switchOn;
    }

    public void setSwitchOn(Boolean switchOn) {
        this.switchOn = switchOn;
    }

    public Double getPowerW() {
        return powerW;
    }

    public void setPowerW(Double powerW) {
        this.powerW = powerW;
    }

    public Double getEnergyKwh() {
        return energyKwh;
    }

    public void setEnergyKwh(Double energyKwh) {
        this.energyKwh = energyKwh;
    }

    public Double getVoltageV() {
        return voltageV;
    }

    public void setVoltageV(Double voltageV) {
        this.voltageV = voltageV;
    }

    public Double getCurrentA() {
        return currentA;
    }

    public void setCurrentA(Double currentA) {
        this.currentA = currentA;
    }

}
