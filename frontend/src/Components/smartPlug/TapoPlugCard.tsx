import "./TapoPlugCard.css";
import { useState, useEffect } from "react";
import api from "../../api.ts";
import type { HaEntityState, TapoPlugEntities } from "./tapoPlugTypes";
import { useTapoPlug } from "./useTapoPlug.ts";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

type TapoChartMetric = "POWER" | "ENERGY" | "VOLTAGE" | "CURRENT";
type TapoChartPeriod = "LAST_HOUR" | "LAST_3_HOURS" | "LAST_24_HOURS";

type TapoChartPoint = {
  timestamp: string;
  value: number;
};

type TapoChartDisplayPoint = {
  timestamp: string;
  timestampMs: number;
  value: number;
};

function isUnavailable(entity: HaEntityState | null): boolean {
  return (
    !entity ||
    entity.state === "unknown" ||
    entity.state === "unavailable" ||
    entity.state === ""
  );
}

function formatValue(
  entity: HaEntityState | null,
  fallbackUnit: string,
  decimals = 1,
): string {
  if (isUnavailable(entity)) {
    return `-- ${fallbackUnit}`;
  }

  const numericValue = Number(entity!.state);

  if (Number.isNaN(numericValue)) {
    return `${entity!.state} ${entity!.attributes?.unit_of_measurement ?? fallbackUnit}`;
  }

  const unit = entity!.attributes?.unit_of_measurement ?? fallbackUnit;

  return `${numericValue.toFixed(decimals)} ${unit}`;
}

function formatUpdatedAt(entity: TapoPlugEntities | null): string {
  const last_update =
    entity?.lastUpdated ?? entity?.voltageEntity?.last_updated;
  if (!last_update) return "Actualizare indisponibilă";

  const date = new Date(last_update);

  return `Ultima actualizare ${date.toLocaleTimeString("ro-RO", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  })}`;
}

interface MetricProps {
  icon: string;
  label: string;
  value: string;
}

function MetricItem({ icon, label, value }: MetricProps) {
  return (
    <div className="col-6 col-lg-3">
      <div className="tapo-metric h-100">
        <div className="tapo-metric-icon">{icon}</div>
        <div>
          <div className="tapo-metric-label">{label}</div>
          <div className="tapo-metric-value">{value}</div>
        </div>
      </div>
    </div>
  );
}

export default function TapoPlugCard() {
  const {
    entities,
    loading,
    error,
    socketStatus,
    commandPending,
    refreshEntities,
    toggle,
  } = useTapoPlug();

  const isOn = entities.switchEntity?.state === "on";
  const isOnline = !isUnavailable(entities.switchEntity);

  const power = formatValue(entities.powerEntity, "W", 1);
  const energy = formatValue(entities.energyEntity, "kWh", 3);
  const voltage = formatValue(entities.voltageEntity, "V", 1);
  const current = formatValue(entities.currentEntity, "A", 3);

  const updatedAt = formatUpdatedAt(entities);

  const [selectedMetric, setSelectedMetric] =
    useState<TapoChartMetric>("POWER");
  const [selectedPeriod, setSelectedPeriod] =
    useState<TapoChartPeriod>("LAST_HOUR");
  const [chartData, setChartData] = useState<TapoChartPoint[]>([]);
  const [chartLoading, setChartLoading] = useState(false);

  useEffect(() => {
    const fetchChartData = async () => {
      try {
        setChartLoading(true);

        const response = await api.get<TapoChartPoint[]>(
          "/api/tapo/history/chart",
          {
            params: {
              metric: selectedMetric,
              period: selectedPeriod,
            },
          },
        );

        setChartNow(Date.now());
        setChartData(response.data);
      } catch (err) {
        console.error("Eroare la încărcarea datelor pentru grafic:", err);
      } finally {
        setChartLoading(false);
      }
    };

    fetchChartData();
  }, [selectedMetric, selectedPeriod]);

  useEffect(() => {
    const liveValue = getLiveValueForMetric(selectedMetric, entities);

    if (liveValue === null) {
      return;
    }

    const now = Date.now();

    const newPoint: TapoChartPoint = {
      timestamp: new Date(now).toISOString(),
      value: liveValue,
    };

    setChartData((previousData) => {
      const lastPoint = previousData[previousData.length - 1];

      if (lastPoint) {
        const lastTime = new Date(lastPoint.timestamp).getTime();
        const now = Date.now();

        if (now - lastTime < MIN_LIVE_POINT_INTERVAL_MS) {
          return previousData;
        }
      }

      setChartNow(now);

      const updatedData = [...previousData, newPoint];

      return trimChartDataByPeriod(updatedData, selectedPeriod);
    });
  }, [entities, selectedMetric, selectedPeriod]);

  const metricLabels: Record<TapoChartMetric, string> = {
    POWER: "Putere",
    ENERGY: "Consum",
    VOLTAGE: "Tensiune",
    CURRENT: "Curent",
  };

  const metricUnits: Record<TapoChartMetric, string> = {
    POWER: "W",
    ENERGY: "kWh",
    VOLTAGE: "V",
    CURRENT: "A",
  };

  const periodLabels: Record<TapoChartPeriod, string> = {
    LAST_HOUR: "Ultima oră",
    LAST_3_HOURS: "Ultimele 3 ore",
    LAST_24_HOURS: "Ultimele 24 de ore",
  };

  const MIN_LIVE_POINT_INTERVAL_MS = 5000;

  const getLiveValueForMetric = (
    metric: TapoChartMetric,
    snapshot: TapoPlugEntities,
  ): number | null => {
    const parseValue = (state?: string | null) => {
      if (!state || state === "unknown" || state === "unavailable") {
        return null;
      }

      const value = Number(state);
      return Number.isNaN(value) ? null : value;
    };

    switch (metric) {
      case "POWER":
        return parseValue(snapshot.powerEntity?.state);

      case "ENERGY":
        return parseValue(snapshot.energyEntity?.state);

      case "VOLTAGE":
        return parseValue(snapshot.voltageEntity?.state);

      case "CURRENT":
        return parseValue(snapshot.currentEntity?.state);

      default:
        return null;
    }
  };

  const trimChartDataByPeriod = (
    data: TapoChartPoint[],
    period: TapoChartPeriod,
  ) => {
    const now = Date.now();

    const periodMs: Record<TapoChartPeriod, number> = {
      LAST_HOUR: 60 * 60 * 1000,
      LAST_3_HOURS: 3 * 60 * 60 * 1000,
      LAST_24_HOURS: 24 * 60 * 60 * 1000,
    };

    const minTime = now - periodMs[period];

    return data.filter(
      (point) => new Date(point.timestamp).getTime() >= minTime,
    );
  };

  const periodMs: Record<TapoChartPeriod, number> = {
    LAST_HOUR: 60 * 60 * 1000,
    LAST_3_HOURS: 3 * 60 * 60 * 1000,
    LAST_24_HOURS: 24 * 60 * 60 * 1000,
  };

  const [chartNow, setChartNow] = useState(Date.now());

  const chartEndTime = chartNow;
  const chartStartTime = chartEndTime - periodMs[selectedPeriod];

  const chartDisplayData: TapoChartDisplayPoint[] = chartData.map((point) => ({
    ...point,
    timestampMs: new Date(point.timestamp).getTime(),
  }));

  const getTickIntervalMs = (period: TapoChartPeriod) => {
    switch (period) {
      case "LAST_HOUR":
        return 10 * 60 * 1000;

      case "LAST_3_HOURS":
        return 30 * 60 * 1000;

      case "LAST_24_HOURS":
        return 3 * 60 * 60 * 1000;

      default:
        return 10 * 60 * 1000;
    }
  };

  const buildTimeTicks = (
    startTime: number,
    endTime: number,
    period: TapoChartPeriod,
  ) => {
    const interval = getTickIntervalMs(period);
    const ticks: number[] = [];

    const firstTick = Math.ceil(startTime / interval) * interval;

    for (let tick = firstTick; tick <= endTime; tick += interval) {
      ticks.push(tick);
    }

    return ticks;
  };

  const xAxisTicks = buildTimeTicks(
    chartStartTime,
    chartEndTime,
    selectedPeriod,
  );

  return (
    <div className="tapo-card card border-0">
      <div className="card-body p-4 p-lg-5">
        <div className="d-flex justify-content-between align-items-start gap-3 mb-4">
          <div className="d-flex align-items-center gap-3">
            <div className="tapo-device-icon">
              <div className="tapo-plug-face">
                <span className="tapo-led" />
                <span className="tapo-hole tapo-hole-left" />
                <span className="tapo-hole tapo-hole-right" />
                <span className="tapo-brand">tapo</span>
              </div>
            </div>

            <div>
              <h3 className="tapo-title mb-1">Priză Tapo P110M</h3>

              <div className="d-flex align-items-center flex-wrap gap-2">
                <span
                  className={`tapo-status-badge ${
                    isOnline ? "tapo-status-online" : "tapo-status-offline"
                  }`}
                >
                  <span className="tapo-status-dot" />
                  {isOnline ? "Online" : "Offline"}
                </span>

                <span className="tapo-ws-status">
                  WebSocket:{" "}
                  <strong
                    className={
                      socketStatus === "connected"
                        ? "text-success"
                        : "text-secondary"
                    }
                  >
                    {socketStatus}
                  </strong>
                </span>
              </div>
            </div>
          </div>

          <button
            type="button"
            className={`tapo-toggle ${isOn ? "tapo-toggle-on" : ""}`}
            onClick={toggle}
            disabled={loading || commandPending || !isOnline}
            aria-label={isOn ? "Oprește priza" : "Pornește priza"}
          >
            <span className="tapo-toggle-knob" />
          </button>
        </div>

        {error && (
          <div className="alert alert-warning py-2 px-3 mb-4" role="alert">
            {error}
          </div>
        )}

        <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
          <div>
            <span className="tapo-live-pill">
              <span className="tapo-live-icon">⚡</span>
              Consum live
            </span>
          </div>

          <button
            type="button"
            className="btn btn-sm btn-outline-secondary tapo-refresh-btn"
            onClick={refreshEntities}
            disabled={loading}
          >
            Reîmprospătează
          </button>
        </div>

        <div className="row g-3 mb-4">
          <MetricItem
            icon="⚡"
            label="Putere"
            value={loading ? "-- W" : power}
          />
          <MetricItem
            icon="▣"
            label="Energie totală"
            value={loading ? "-- kWh" : energy}
          />
          <MetricItem
            icon="V"
            label="Tensiune"
            value={loading ? "-- V" : voltage}
          />
          <MetricItem
            icon="A"
            label="Curent"
            value={loading ? "-- A" : current}
          />
        </div>

        <div className="tapo-chart-card">
          <div className="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-3">
            <div>
              <div className="tapo-chart-title">
                {metricLabels[selectedMetric]} · {periodLabels[selectedPeriod]}
              </div>

              <div className="tapo-chart-subtitle">
                Valori salvate în baza de date pentru priza Tapo.
              </div>
            </div>

            <div className="d-flex flex-column align-items-end gap-2">
              <div className="tapo-range-pills">
                <button
                  className={selectedMetric === "POWER" ? "active" : ""}
                  type="button"
                  onClick={() => setSelectedMetric("POWER")}
                >
                  Putere
                </button>

                <button
                  className={selectedMetric === "ENERGY" ? "active" : ""}
                  type="button"
                  onClick={() => setSelectedMetric("ENERGY")}
                >
                  Consum
                </button>

                <button
                  className={selectedMetric === "VOLTAGE" ? "active" : ""}
                  type="button"
                  onClick={() => setSelectedMetric("VOLTAGE")}
                >
                  Tensiune
                </button>

                <button
                  className={selectedMetric === "CURRENT" ? "active" : ""}
                  type="button"
                  onClick={() => setSelectedMetric("CURRENT")}
                >
                  Curent
                </button>
              </div>

              <div className="tapo-range-pills">
                <button
                  className={selectedPeriod === "LAST_HOUR" ? "active" : ""}
                  type="button"
                  onClick={() => setSelectedPeriod("LAST_HOUR")}
                >
                  1h
                </button>

                <button
                  className={selectedPeriod === "LAST_3_HOURS" ? "active" : ""}
                  type="button"
                  onClick={() => setSelectedPeriod("LAST_3_HOURS")}
                >
                  3h
                </button>

                <button
                  className={selectedPeriod === "LAST_24_HOURS" ? "active" : ""}
                  type="button"
                  onClick={() => setSelectedPeriod("LAST_24_HOURS")}
                >
                  24h
                </button>
              </div>
            </div>
          </div>

          <div className="tapo-chart-placeholder">
            {chartLoading ? (
              <div className="tapo-chart-empty">Se încarcă datele...</div>
            ) : chartData.length === 0 ? (
              <div className="tapo-chart-empty">
                Nu există date pentru perioada selectată.
              </div>
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <LineChart data={chartDisplayData}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />

                  <XAxis
                    dataKey="timestampMs"
                    type="number"
                    domain={[chartStartTime, chartEndTime]}
                    scale="time"
                    ticks={xAxisTicks}
                    tickFormatter={(value) =>
                      new Date(value).toLocaleTimeString("ro-RO", {
                        hour: "2-digit",
                        minute: "2-digit",
                      })
                    }
                    minTickGap={32}
                  />

                  <YAxis
                    tickFormatter={(value) =>
                      `${value} ${metricUnits[selectedMetric]}`
                    }
                    width={70}
                  />

                  <Tooltip
                    labelFormatter={(value) =>
                      new Date(Number(value)).toLocaleString("ro-RO")
                    }
                    formatter={(value) => [
                      `${Number(value).toFixed(2)} ${metricUnits[selectedMetric]}`,
                      metricLabels[selectedMetric],
                    ]}
                  />

                  <Line
                    isAnimationActive={false}
                    type="monotone"
                    dataKey="value"
                    dot={false}
                    strokeWidth={2.5}
                    activeDot={{ r: 5 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mt-4 tapo-footer">
          <span>{updatedAt}</span>

          <span className={isOn ? "text-success" : "text-secondary"}>
            {isOn ? "Priza este pornită" : "Priza este oprită"}
          </span>
        </div>
      </div>
    </div>
  );
}
