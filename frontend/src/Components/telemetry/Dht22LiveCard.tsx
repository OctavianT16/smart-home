import React from "react";
import { useDht22Telemetry, type Dht22Telemetry } from "./useDht22Telemetry";
import "./Dht22LiveCard.css";

function formatTs(ts?: number) {
  if (!ts) return "-";
  return new Date(ts).toLocaleString();
}

function statusBadgeClass(state: string) {
  switch (state) {
    case "connected":
      return "bg-success";
    case "connecting":
      return "bg-warning text-dark";
    case "error":
      return "bg-danger";
    default:
      return "bg-secondary";
  }
}

export const Dht22LiveCard: React.FC<{
  backendBaseUrl?: string;
  deviceId: string;
  lastMessage: Dht22Telemetry | null;
}> = ({ backendBaseUrl, deviceId, lastMessage }) => {
  const { connectionState, lastError } = useDht22Telemetry({
    backendBaseUrl,
    deviceId,
  });

  return (
    <div className="card shadow-sm dht22-card">
      <div className="card-body">
        <div className="d-flex align-items-start justify-content-between gap-3">
          <div>
            <h5 className="card-title mb-1">Temperatură și umiditate</h5>
            <div className="text-muted small">
              Device:{" "}
              <span className="fw-semibold">
                {lastMessage?.deviceId ?? deviceId}
              </span>
              {" · "}
              Sensor:{" "}
              <span className="fw-semibold">
                {lastMessage?.sensor ?? "dht22"}
              </span>
            </div>
          </div>

          <span
            className={`badge rounded-pill ${statusBadgeClass(
              connectionState,
            )}`}
          >
            {connectionState}
          </span>
        </div>

        {lastError && (
          <div className="alert alert-danger py-2 px-3 mt-3 mb-0 dht22-alert">
            <div className="small">{lastError}</div>
          </div>
        )}

        <div className="row g-3 mt-1">
          <div className="col-12 col-sm-6">
            <div className="dht22-metric card h-100">
              <div className="card-body">
                <div className="text-muted small">Temperature</div>
                <div className="dht22-value">
                  {lastMessage?.temperatureC != null ? (
                    <>
                      {lastMessage.temperatureC.toFixed(1)}{" "}
                      <span className="dht22-unit">°C</span>
                    </>
                  ) : (
                    <span className="text-muted">—</span>
                  )}
                </div>
                <div className="small text-muted mt-1">Live update</div>
              </div>
            </div>
          </div>

          <div className="col-12 col-sm-6">
            <div className="dht22-metric card h-100">
              <div className="card-body">
                <div className="text-muted small">Humidity</div>
                <div className="dht22-value">
                  {lastMessage?.humidityPct != null ? (
                    <>
                      {lastMessage.humidityPct.toFixed(1)}{" "}
                      <span className="dht22-unit">%</span>
                    </>
                  ) : (
                    <span className="text-muted">—</span>
                  )}
                </div>
                <div className="small text-muted mt-1">Live update</div>
              </div>
            </div>
          </div>
        </div>

        <div className="d-flex flex-wrap align-items-center justify-content-between mt-3 pt-2 border-top">
          <div className="text-muted small">
            Last data:{" "}
            <span className="fw-semibold">{formatTs(lastMessage?.ts)}</span>
          </div>

          <div className="dht22-dotline small text-muted">
            <span
              className={`dht22-dot ${
                connectionState === "connected" ? "is-on" : ""
              }`}
            />
            <span className="ms-2">Realtime stream</span>
          </div>
        </div>
      </div>
    </div>
  );
};
