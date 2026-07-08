import { useState, useEffect } from "react";
import axios from "axios";
import type {
  CountdownValue,
  FanSpeed,
  TuyaMode,
  DehumidifierStatus,
} from "./dehumidifierApi";

import {
  getDehumidifierStatus,
  setDehumidifierCountdown,
  setDehumidifierFanSpeed,
  setDehumidifierHumidity,
  setDehumidifierMode,
  turnOffDehumidifier,
  turnOnDehumidifier,
} from "./dehumidifierApi";

import "./DehumidifierRemote.css";

const modes: { value: TuyaMode; label: string }[] = [
  { value: "manual", label: "Manual" },
  { value: "auto", label: "Auto" },
  { value: "laundry", label: "Uscare rufe" },
  { value: "purify", label: "Purificare" },
  { value: "sleep", label: "Noapte" },
];

const fanSpeeds: { value: FanSpeed; label: string }[] = [
  { value: "low", label: "Mică" },
  { value: "mid", label: "Medie" },
  { value: "high", label: "Mare" },
];

const countdownValues: { value: CountdownValue; label: string }[] = [
  { value: "cancel", label: "Fără timer" },
  { value: "1h", label: "1h" },
  { value: "2h", label: "2h" },
  { value: "4h", label: "4h" },
  { value: "6h", label: "6h" },
  { value: "8h", label: "8h" },
];

export default function DehumidifierRemote() {
  const [isOn, setIsOn] = useState(false);
  const [statusLoading, setStatusLoading] = useState(true);
  const [mode, setMode] = useState<TuyaMode>("manual");
  const [humidity, setHumidity] = useState<number>(50);
  const [fanSpeed, setFanSpeed] = useState<FanSpeed>("mid");
  const [countdown, setCountdown] = useState<CountdownValue>("cancel");

  const [commandPending, setCommandPending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!error && !successMessage) {
      return;
    }

    const timeoutId = window.setTimeout(() => {
      setError(null);
      setSuccessMessage(null);
    }, 5000);

    return () => {
      window.clearTimeout(timeoutId);
    };
  }, [error, successMessage]);

  const handleError = (err: unknown) => {
    console.error(err);

    if (axios.isAxiosError(err) && err.response?.data) {
      setError(String(err.response.data));
    } else {
      setError("A apărut o eroare la trimiterea comenzii.");
    }
  };

  const runCommand = async (
    command: () => Promise<unknown>,
    successText: string,
    onSuccess?: () => void,
  ) => {
    try {
      setCommandPending(true);
      setError(null);
      setSuccessMessage(null);

      await command();

      if (onSuccess) {
        onSuccess();
      }

      setSuccessMessage(successText);
    } catch (err) {
      handleError(err);
    } finally {
      setCommandPending(false);
    }
  };

  const handlePowerToggle = async () => {
    const nextState = !isOn;

    await runCommand(
      () => {
        if (nextState) {
          return turnOnDehumidifier();
        }

        return turnOffDehumidifier();
      },
      nextState ? "Dezumidificator pornit." : "Dezumidificator oprit.",
      () => setIsOn(nextState),
    );
  };

  const applyMode = async (nextMode: TuyaMode) => {
    await runCommand(
      () => setDehumidifierMode(nextMode),
      `Mod setat: ${modes.find((item) => item.value === nextMode)?.label}.`,
      () => setMode(nextMode),
    );
  };

  const applyFanSpeed = async (nextFanSpeed: FanSpeed) => {
    await runCommand(
      () => setDehumidifierFanSpeed(nextFanSpeed),
      `Viteza ventilatorului a fost setată: ${
        fanSpeeds.find((item) => item.value === nextFanSpeed)?.label
      }.`,
      () => setFanSpeed(nextFanSpeed),
    );
  };

  const applyCountdown = async (nextCountdown: CountdownValue) => {
    await runCommand(
      () => setDehumidifierCountdown(nextCountdown),
      nextCountdown === "cancel"
        ? "Timer anulat."
        : `Timer setat la ${nextCountdown}.`,
      () => setCountdown(nextCountdown),
    );
  };

  const applyStatusToState = (status: DehumidifierStatus) => {
    if (typeof status.power === "boolean") {
      setIsOn(status.power);
    }

    if (status.mode) {
      setMode(status.mode);
    }

    if (typeof status.humidity === "number") {
      setHumidity(status.humidity);
    }

    if (status.fanSpeed) {
      setFanSpeed(status.fanSpeed);
    }

    if (status.countdown) {
      setCountdown(status.countdown);
    }
  };

  const fetchStatus = async () => {
    try {
      const status = await getDehumidifierStatus();
      applyStatusToState(status);
    } catch (err) {
      console.error("Nu s-a putut citi statusul dezumidificatorului:", err);
    } finally {
      setStatusLoading(false);
    }
  };

  useEffect(() => {
    fetchStatus();
  }, []);

  useEffect(() => {
    const intervalId = window.setInterval(() => {
      fetchStatus();
    }, 10000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, []);

  return (
    <div className="dehumidifier-card">
      <div className="dehumidifier-header">
        <div className="d-flex align-items-center gap-3">
          <div className="dehumidifier-device-icon">
            <div className="dehumidifier-drop">💧</div>
            <span>tuya</span>
          </div>

          <div>
            <h2 className="dehumidifier-title">Dezumidificator Tuya</h2>

            <div className="d-flex align-items-center gap-2 flex-wrap">
              <span
                className={
                  isOn
                    ? "dehumidifier-status-pill online"
                    : "dehumidifier-status-pill offline"
                }
              >
                <span className="status-dot"></span>
                {isOn ? "Pornit" : "Oprit"}
              </span>

              <span className="dehumidifier-subtitle">
                Control prin Tuya Cloud
              </span>
            </div>
          </div>
        </div>

        <div className="dehumidifier-power-area">
          <div className="form-check form-switch dehumidifier-switch">
            <input
              className="form-check-input"
              type="checkbox"
              role="switch"
              checked={isOn}
              disabled={commandPending}
              onChange={handlePowerToggle}
            />
          </div>
        </div>
      </div>

      {(error || successMessage || commandPending) && (
        <div className="dehumidifier-feedback">
          {error && (
            <div className="alert alert-danger py-2 mb-2" role="alert">
              {error}
            </div>
          )}

          {successMessage && (
            <div className="alert alert-success py-2 mb-2" role="alert">
              {successMessage}
            </div>
          )}

          {commandPending && (
            <div className="text-muted small">Se trimite comanda...</div>
          )}
        </div>
      )}

      <div className="dehumidifier-live-pill">
        <span className="dehumidifier-live-icon">☁</span>
        <span>Control live</span>
      </div>

      <div className="dehumidifier-metrics-grid">
        <div className="dehumidifier-info-box">
          <div className="metric-icon">%</div>
          <div>
            <span className="metric-label">Umiditate țintă</span>
            <strong>{humidity}%</strong>
          </div>
        </div>

        <div className="dehumidifier-info-box">
          <div className="metric-icon">≋</div>
          <div>
            <span className="metric-label">Ventilator</span>
            <strong>
              {fanSpeeds.find((item) => item.value === fanSpeed)?.label}
            </strong>
          </div>
        </div>

        <div className="dehumidifier-info-box">
          <div className="metric-icon">⌁</div>
          <div>
            <span className="metric-label">Mod</span>
            <strong>{modes.find((item) => item.value === mode)?.label}</strong>
          </div>
        </div>

        <div className="dehumidifier-info-box">
          <div className="metric-icon">⏱</div>
          <div>
            <span className="metric-label">Timer</span>
            <strong>
              {countdownValues.find((item) => item.value === countdown)?.label}
            </strong>
          </div>
        </div>
      </div>

      <div className="dehumidifier-control-panel">
        <div className="control-panel-header">
          <div>
            <h5>Setări dezumidificator</h5>
          </div>
        </div>

        <div className="control-section">
          <div className="section-label">Mod funcționare</div>

          <div className="segmented-control">
            {modes.map((item) => (
              <button
                key={item.value}
                type="button"
                disabled={commandPending}
                className={mode === item.value ? "active" : ""}
                onClick={() => applyMode(item.value)}
              >
                {item.label}
              </button>
            ))}
          </div>
        </div>

        <div className="control-section">
          <div className="d-flex justify-content-between align-items-center mb-2">
            <div className="section-label">Umiditate țintă</div>
            <strong className="humidity-value">{humidity}%</strong>
          </div>

          <input
            type="range"
            className="form-range dehumidifier-range"
            min={25}
            max={80}
            step={5}
            value={humidity}
            disabled={commandPending}
            onChange={(e) => setHumidity(Number(e.target.value))}
          />

          <div className="d-flex justify-content-between text-muted small">
            <span>25%</span>
            <span>80%</span>
          </div>

          <button
            className="apply-button mt-3"
            type="button"
            disabled={commandPending}
            onClick={() =>
              runCommand(
                () => setDehumidifierHumidity(humidity),
                `Umiditate setată la ${humidity}%.`,
                () => setHumidity(humidity),
              )
            }
          >
            Aplică umiditatea
          </button>
        </div>

        <div className="row g-3">
          <div className="col-lg-6">
            <div className="control-section h-100">
              <div className="section-label">Viteză ventilator</div>

              <div className="segmented-control mt-2">
                {fanSpeeds.map((item) => (
                  <button
                    key={item.value}
                    type="button"
                    disabled={commandPending}
                    className={fanSpeed === item.value ? "active" : ""}
                    onClick={() => applyFanSpeed(item.value)}
                  >
                    {item.label}
                  </button>
                ))}
              </div>
            </div>
          </div>

          <div className="col-lg-6">
            <div className="control-section h-100">
              <div className="section-label">Timer</div>

              <div className="segmented-control timer-control mt-2">
                {countdownValues.map((item) => (
                  <button
                    key={item.value}
                    type="button"
                    disabled={commandPending}
                    className={countdown === item.value ? "active" : ""}
                    onClick={() => applyCountdown(item.value)}
                  >
                    {item.label}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="dehumidifier-footer">
        <strong>{isOn ? "Dispozitiv pornit" : "Dispozitiv oprit"}</strong>
      </div>
    </div>
  );
}
