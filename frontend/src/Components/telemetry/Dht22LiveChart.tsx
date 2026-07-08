import React from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";
import type { Dht22Point } from "./useDht22Telemetry";

export const Dht22LiveChart: React.FC<{
  data: Dht22Point[];
  height?: number;
  windowMinutes: number;
}> = ({ data, height = 280, windowMinutes }) => {
  const now = Date.now() + 60000 * 5;
  const start = now - windowMinutes * 60_000;

  const formatTime = (value: number) => {
    return new Date(value).toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const formatTooltipTime = (value: number) => {
    return new Date(value).toLocaleString([], {
      day: "2-digit",
      month: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    });
  };

  return (
    <div className="card shadow-sm">
      <div className="card-body">
        <div className="d-flex align-items-center justify-content-between mb-2">
          <h5 className="mb-0">Istoric temperatură și umiditate</h5>
          <span className="text-muted small">{data.length} points</span>
        </div>

        <div style={{ width: "100%", height }}>
          <ResponsiveContainer>
            <LineChart
              data={data}
              margin={{ top: 10, right: 16, left: 0, bottom: 8 }}
            >
              <CartesianGrid strokeDasharray="3 3" vertical={false} />

              <XAxis
                dataKey="ts"
                type="number"
                scale="time"
                domain={[start, now]}
                tickFormatter={(value) => formatTime(Number(value))}
                minTickGap={36}
                // tickCount={10}
                ticks={data.map((point) => point.ts)}
              />

              <YAxis
                yAxisId="left"
                tickCount={5}
                width={44}
                domain={["dataMin - 0.5", "dataMax + 0.5"]}
                tickFormatter={(value) => Number(value).toFixed(1)}
              />

              <YAxis
                yAxisId="right"
                orientation="right"
                tickCount={5}
                width={44}
                domain={["dataMin - 2", "dataMax + 2"]}
                tickFormatter={(value) => Number(value).toFixed(0)}
              />

              <Tooltip
                labelFormatter={(value) => formatTooltipTime(Number(value))}
                formatter={(value, name) => {
                  if (name === "Temp (°C)") {
                    return [`${Number(value).toFixed(1)} °C`, name];
                  }

                  if (name === "Humidity (%)") {
                    return [`${Number(value).toFixed(1)} %`, name];
                  }

                  return [value, name];
                }}
              />

              <Legend />

              <Line
                yAxisId="left"
                type="monotone"
                dataKey="temperatureC"
                name="Temp (°C)"
                stroke="red"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4 }}
                isAnimationActive={false}
              />

              <Line
                yAxisId="right"
                type="monotone"
                dataKey="humidityPct"
                name="Humidity (%)"
                stroke="blue"
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4 }}
                isAnimationActive={false}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};
