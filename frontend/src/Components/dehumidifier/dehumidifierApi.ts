import api from "../../api";

export type TuyaMode = "manual" | "auto" | "laundry" | "purify" | "sleep";

export type FanSpeed = "low" | "mid" | "high";

export type CountdownValue = "cancel" | "1h" | "2h" | "4h" | "6h" | "8h";

export type DehumidifierStatus = {
  power: boolean | null;
  mode: TuyaMode | null;
  humidity: number | null;
  fanSpeed: FanSpeed | null;
  countdown: CountdownValue | null;
};

export const getDehumidifierStatus = async () => {
  const response = await api.get<DehumidifierStatus>(
    "/api/dehumidifier/status",
  );
  return response.data;
};

export const turnOnDehumidifier = async () => {
  return api.post("/api/dehumidifier/on");
};

export const turnOffDehumidifier = async () => {
  return api.post("/api/dehumidifier/off");
};

export const setDehumidifierMode = async (mode: TuyaMode) => {
  return api.post(`/api/dehumidifier/mode/${mode}`);
};

export const setDehumidifierHumidity = async (humidity: number) => {
  return api.post(`/api/dehumidifier/humidity/${humidity}`);
};

export const setDehumidifierFanSpeed = async (fanSpeed: FanSpeed) => {
  return api.post(`/api/dehumidifier/fan-speed/${fanSpeed}`);
};

export const setDehumidifierCountdown = async (countdown: CountdownValue) => {
  return api.post(`/api/dehumidifier/countdown/${countdown}`);
};
