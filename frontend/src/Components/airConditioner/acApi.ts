import api from "../../api";

const postJson = async (path: string, body: unknown) => {
  try {
    const res = await api.post(path, body, {
      withCredentials: true,
      headers: { "Content-Type": "application/json" },
    });

    return res.data;
  } catch (error) {
    console.error("Error in postJson:", error);
    throw error;
  }
};

export type AcMode = "COOL" | "HEAT" | "FAN" | "DRY" | "AUTO";

export type AcState = {
  power: boolean;
  mode: AcMode;
  temperatureC: number;
  fanLevel: number;
  autoEnabled: boolean;
  targetAmbientTemperatureC: number;
  lastUpdatedBy: string;
};

export const acApi = {
  power: (on: boolean) => postJson("/api/ac/power", { on }),

  mode: (mode: AcMode) => postJson("/api/ac/mode", { mode }),

  temperature: (celsius: number) =>
    postJson("/api/ac/temperature", { celsius }),

  fan: (level: number) => postJson("/api/ac/fan", { level }),

  auto: (enabled: boolean) => postJson("/api/ac/auto", { enabled }),

  targetAmbientTemperature: (targetC: number) =>
    postJson("/api/ac/target", { targetCelsius: targetC }),

  state: async (): Promise<AcState> => {
    const res = await api.get<AcState>("/api/ac/state", {
      withCredentials: true,
    });

    return res.data;
  },
};
