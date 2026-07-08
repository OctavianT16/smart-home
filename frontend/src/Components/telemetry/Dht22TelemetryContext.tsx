import React, { createContext, useContext, useMemo, useState } from "react";
import {
  useDht22Telemetry,
  type Dht22Point,
  type Dht22Telemetry,
} from "./useDht22Telemetry";

type ConnectionState = "disconnected" | "connecting" | "connected" | "error";

type Dht22Ctx = {
  connectionState: ConnectionState;
  lastError: string | null;
  series: Dht22Point[];
  clearSeries: () => void;
  historyLoading: boolean;
  lastMessage: Dht22Telemetry | null;

  windowMinutes: number;
  setWindowMinutes: React.Dispatch<React.SetStateAction<number>>;
  maxPoints: number;
};

const Dht22TelemetryContext = createContext<Dht22Ctx | null>(null);

export function Dht22TelemetryProvider(props: {
  children: React.ReactNode;
  backendBaseUrl?: string;
  deviceId: string;
}) {
  const { children, backendBaseUrl = "", deviceId } = props;

  const [windowMinutes, setWindowMinutes] = useState(60);

  const maxPoints = useMemo(() => {
    if (windowMinutes === 60) return 2000;
    if (windowMinutes === 180) return 5000;
    return 15000;
  }, [windowMinutes]);

  const {
    connectionState,
    lastError,
    series,
    clearSeries,
    historyLoading,
    lastMessage,
  } = useDht22Telemetry({
    backendBaseUrl,
    deviceId,
    windowMinutes,
    maxPoints,
  });

  const value = useMemo<Dht22Ctx>(
    () => ({
      connectionState,
      lastError,
      series,
      clearSeries,
      historyLoading,
      windowMinutes,
      setWindowMinutes,
      maxPoints,
      lastMessage,
    }),
    [connectionState, lastError, series, windowMinutes, maxPoints],
  );

  return (
    <Dht22TelemetryContext.Provider value={value}>
      {children}
    </Dht22TelemetryContext.Provider>
  );
}

export function useDht22() {
  const ctx = useContext(Dht22TelemetryContext);
  if (!ctx)
    throw new Error("useDht22 must be used inside Dht22TelemetryProvider");
  return ctx;
}
