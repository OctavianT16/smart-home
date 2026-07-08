import { useEffect, useRef, useState } from "react";
import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import api from "../../api";

export type Dht22Telemetry = {
  deviceId: string;
  sensor: "dht22" | string;
  temperatureC: number;
  humidityPct: number;
  ts: number;
};

type ConnectionState = "disconnected" | "connecting" | "connected" | "error";

export type Dht22Point = {
  ts: number;
  label: string;
  temperatureC: number;
  humidityPct: number;
};

type Dht22HistoryPointResponse = {
  ts: number;
  temperatureC: number;
  humidityPct: number;
};

function toPoint(item: Dht22HistoryPointResponse): Dht22Point {
  return {
    ts: item.ts,
    label: makeLabel(item.ts),
    temperatureC: item.temperatureC,
    humidityPct: item.humidityPct,
  };
}

function makeLabel(ts: number) {
  return new Date(ts).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

export function useDht22Telemetry(params: {
  backendBaseUrl?: string;
  deviceId: string;

  maxPoints?: number;
  windowMinutes?: number;
}) {
  const {
    backendBaseUrl = "",
    deviceId,
    maxPoints = 60,
    windowMinutes = 10,
  } = params;

  const [connectionState, setConnectionState] =
    useState<ConnectionState>("disconnected");
  const [lastMessage, setLastMessage] = useState<Dht22Telemetry | null>(null);
  const [lastError, setLastError] = useState<string | null>(null);
  const [series, setSeries] = useState<Dht22Point[]>([]);
  const [historyLoading, setHistoryLoading] = useState(false);

  const clientRef = useRef<Client | null>(null);
  const now = Date.now();
  const cutoff = now - windowMinutes * 60_000;
  const lastLivePointAddedAtRef = useRef<number>(0);
  const LIVE_CHART_POINT_INTERVAL_MS = 10_000;

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        setHistoryLoading(true);
        setLastError(null);

        const baseUrl = backendBaseUrl || "";

        const response = await api.get<Dht22HistoryPointResponse[]>(
          `${baseUrl}/api/dht22/${deviceId}/history`,
          {
            params: {
              minutes: windowMinutes,
            },
          },
        );

        const points = response.data
          .map(toPoint)
          .filter((p) => p.ts >= cutoff)
          .sort((a, b) => a.ts - b.ts)
          .slice(-maxPoints);

        setSeries(points);

        console.log("[DHT22 HISTORY] Loaded points:", points.length);
      } catch (error) {
        console.error("[DHT22 HISTORY] Failed to load history:", error);
        setLastError("Failed to load DHT22 history.");
      } finally {
        setHistoryLoading(false);
      }
    };

    fetchHistory();
  }, [backendBaseUrl, deviceId, windowMinutes, maxPoints]);

  useEffect(() => {
    setConnectionState("connecting");
    setLastError(null);

    const topic = `/topic/telemetry/dht22/${deviceId}`;

    const wsUrl = backendBaseUrl ? `${backendBaseUrl}/ws` : "/ws";

    const client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 2000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        setConnectionState("connected");
        setLastError(null);

        client.subscribe(topic, (msg: IMessage) => {
          try {
            const parsed = JSON.parse(msg.body) as Dht22Telemetry;

            setLastMessage(parsed);
            const now = Date.now();

            if (
              lastLivePointAddedAtRef.current !== 0 &&
              now - lastLivePointAddedAtRef.current <
                LIVE_CHART_POINT_INTERVAL_MS
            ) {
              return;
            }

            lastLivePointAddedAtRef.current = now;
            const cutoff = now - windowMinutes * 60_000;

            setSeries((prev) => {
              const nextPoint: Dht22Point = {
                ts: parsed.ts,
                label: makeLabel(parsed.ts),
                temperatureC: parsed.temperatureC,
                humidityPct: parsed.humidityPct,
              };

              const merged = [...prev, nextPoint]
                .filter((p) => p.ts >= cutoff)
                .sort((a, b) => a.ts - b.ts);
              // .slice(-maxPoints);

              return merged;
            });
          } catch (e) {
            setLastError("Failed to parse telemetry JSON.");
          }
        });
      },

      onStompError: (frame) => {
        setConnectionState("error");
        setLastError(frame.headers["message"] || "STOMP error");
      },
      onWebSocketError: () => {
        setConnectionState("error");
        setLastError("WebSocket error (check backend / proxy / endpoint).");
      },
      onDisconnect: () => {
        setConnectionState("disconnected");
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      try {
        client.deactivate();
      } finally {
        clientRef.current = null;
      }
    };
  }, [backendBaseUrl, deviceId, maxPoints, windowMinutes]);

  const clearSeries = () => setSeries([]);

  return {
    connectionState,
    lastMessage,
    lastError,
    series,
    clearSeries,
    historyLoading,
  };
}
