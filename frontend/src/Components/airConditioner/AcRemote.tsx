import React, { useMemo, useState, useEffect } from "react";
import { acApi, type AcMode } from "./acApi";
import { useAcState } from "./useAcState";

type ActionState = "idle" | "sending" | "ok" | "error";

function modeIcon(mode: AcMode) {
  switch (mode) {
    case "COOL":
      return "❄️";
    case "HEAT":
      return "🔥";
    case "FAN":
      return "💨";
    case "DRY":
      return "💧";
    case "AUTO":
      return "🤖";
  }
}

export const AcRemote: React.FC = () => {
  const [power, setPower] = useState(false);
  const [mode, setMode] = useState<AcMode>("COOL");
  const [temp, setTemp] = useState(24);
  const [fan, setFan] = useState(3);
  const [autoEnabled, setAutoEnabled] = useState(false);
  const [targetTemp, setTargetTemp] = useState(24);
  const [update, setUpdate] = useState<string | null>(null);
  const [state, setState] = useState<ActionState>("idle");
  const [error, setError] = useState<string | null>(null);

  const canSend = state !== "sending";

  const badgeClass = useMemo(() => {
    if (state === "ok") return "text-bg-success";
    if (state === "error") return "text-bg-danger";
    if (state === "sending") return "text-bg-warning";
    return "text-bg-secondary";
  }, [state]);

  async function run(label: string, fn: () => Promise<void>) {
    if (!canSend) return;

    setState("sending");
    setError(null);

    try {
      await fn();
      setState("ok");
      window.setTimeout(() => setState("idle"), 1400);
    } catch (e: any) {
      setState("error");
      setError(`${label}: ${e?.message ?? "Unknown error"}`);
    }
  }

  useEffect(() => {
    (async () => {
      try {
        const st = await acApi.state();

        setPower(st.power);
        setMode(st.mode);
        setTemp(st.temperatureC);
        setFan(st.fanLevel);
        setAutoEnabled(st.autoEnabled);
        setTargetTemp(st.targetAmbientTemperatureC ?? 24);
        setUpdate(st.lastUpdatedBy);
      } catch (e) {
        console.error("Failed to load AC state:", e);
      }
    })();
  }, []);

  const { state: wsState } = useAcState();

  useEffect(() => {
    if (!wsState) return;

    setPower(wsState.power);
    setMode(wsState.mode);
    setTemp(wsState.temperatureC);
    setFan(wsState.fanLevel);
    setAutoEnabled(wsState.autoEnabled);
    setTargetTemp(wsState.targetAmbientTemperatureC ?? 24);
    setUpdate(wsState.lastUpdatedBy);
  }, [wsState]);

  const upper = (value: string | null) => {
    if (value != null) return value.toUpperCase();
    return "-";
  };

  return (
    <div className="card shadow-sm h-100">
      <div className="card-body">
        <div className="d-flex align-items-start justify-content-between gap-2">
          <div>
            <h5 className="mb-1">AC Remote</h5>
            <div className="text-muted small">
              Tosot/Gree control (LAN via backend)
            </div>
            <span>Last updated by: {upper(update)}</span>
          </div>

          <span className={`badge ${badgeClass}`}>{state}</span>
        </div>

        {error && (
          <div className="alert alert-danger py-2 mt-3 mb-0">
            <div className="small">{error}</div>
          </div>
        )}

        {/* POWER */}
        <div className="mt-3 d-flex gap-2">
          <button
            className={`btn ${
              power ? "btn-danger" : "btn-outline-danger"
            } flex-grow-1`}
            disabled={!canSend}
            onClick={() =>
              run("Power", async () => {
                const next = !power;
                await acApi.power(next);
                setPower(next);
                setUpdate("UI");
              })
            }
          >
            ⏻ Power
          </button>
        </div>

        {/* SMART THERMOSTAT */}
        <div className="mt-3 p-3 rounded border bg-light">
          <div className="d-flex justify-content-between align-items-start gap-2">
            <div>
              <div className="fw-semibold">Smart thermostat</div>
            </div>

            <button
              className={`btn btn-sm ${
                autoEnabled ? "btn-primary" : "btn-outline-primary"
              }`}
              disabled={!canSend}
              onClick={() =>
                run("Smart thermostat", async () => {
                  const next = !autoEnabled;
                  await acApi.auto(next);
                  setAutoEnabled(next);
                  setUpdate("UI");
                })
              }
            >
              {autoEnabled ? "ON" : "OFF"}
            </button>
          </div>

          <div className="mt-3">
            <div className="d-flex justify-content-between align-items-center">
              <div className="text-muted small">Target room temperature</div>
              <div className="fw-semibold">{targetTemp} °C</div>
            </div>

            <input
              className="form-range"
              type="range"
              min={18}
              max={28}
              step={1}
              value={targetTemp}
              onChange={(e) => setTargetTemp(parseInt(e.target.value, 10))}
              onMouseUp={() =>
                run("Target temperature", async () => {
                  await acApi.targetAmbientTemperature(targetTemp);
                  setUpdate("UI");
                })
              }
              onTouchEnd={() =>
                run("Target temperature", async () => {
                  await acApi.targetAmbientTemperature(targetTemp);
                  setUpdate("UI");
                })
              }
              disabled={!canSend}
            />
          </div>
        </div>

        {/* MODES */}
        <div className="mt-3">
          <div className="text-muted small mb-2">Mode</div>

          <div
            className="d-grid"
            style={{ gridTemplateColumns: "repeat(3, 1fr)", gap: 8 }}
          >
            {(["COOL", "HEAT", "FAN", "DRY", "AUTO"] as AcMode[]).map((m) => (
              <button
                key={m}
                className={`btn btn-sm ${
                  mode === m ? "btn-dark" : "btn-outline-dark"
                }`}
                disabled={!canSend}
                onClick={() =>
                  run("Mode", async () => {
                    await acApi.mode(m);
                    setMode(m);
                    setUpdate("UI");
                  })
                }
                title={m}
              >
                <span className="me-1">{modeIcon(m)}</span>
                {m}
              </button>
            ))}
          </div>
        </div>

        {/* AC TEMPERATURE */}
        <div className="mt-4">
          <div className="d-flex justify-content-between align-items-center">
            <div className="text-muted small">AC set temperature</div>
            <div className="fw-semibold">{temp} °C</div>
          </div>

          <input
            className="form-range"
            type="range"
            min={16}
            max={30}
            step={1}
            value={temp}
            onChange={(e) => setTemp(parseInt(e.target.value, 10))}
            onMouseUp={() =>
              run("Temp", async () => {
                await acApi.temperature(temp);
                setUpdate("UI");
              })
            }
            onTouchEnd={() =>
              run("Temp", async () => {
                await acApi.temperature(temp);
                setUpdate("UI");
              })
            }
            disabled={!canSend}
          />

          <div className="d-flex gap-2">
            <button
              className="btn btn-outline-secondary btn-sm flex-grow-1"
              disabled={!canSend || temp <= 16}
              onClick={() =>
                run("Temp-", async () => {
                  const next = Math.max(16, temp - 1);
                  await acApi.temperature(next);
                  setTemp(next);
                  setUpdate("UI");
                })
              }
            >
              −
            </button>

            <button
              className="btn btn-outline-secondary btn-sm flex-grow-1"
              disabled={!canSend || temp >= 30}
              onClick={() =>
                run("Temp+", async () => {
                  const next = Math.min(30, temp + 1);
                  await acApi.temperature(next);
                  setTemp(next);
                  setUpdate("UI");
                })
              }
            >
              +
            </button>
          </div>
        </div>

        {/* FAN */}
        <div className="mt-4">
          <div className="d-flex justify-content-between align-items-center">
            <div className="text-muted small">Fan</div>
            <div className="fw-semibold">Level {fan}</div>
          </div>

          <div className="btn-group w-100 mt-2" role="group" aria-label="Fan">
            {[1, 2, 3, 4, 5].map((lvl) => (
              <button
                key={lvl}
                className={`btn ${
                  fan === lvl ? "btn-success" : "btn-outline-success"
                }`}
                disabled={!canSend}
                onClick={() =>
                  run("Fan", async () => {
                    await acApi.fan(lvl);
                    setFan(lvl);
                    setUpdate("UI");
                  })
                }
              >
                {lvl}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
