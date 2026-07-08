import React, {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import axios from "axios";
import imgClear from "../../assets/weather/clear.png";
import imgCloudy from "../../assets/weather/cloudy.png";
import imgRain from "../../assets/weather/rain.png";
import imgSnow from "../../assets/weather/snow.png";
import imgFog from "../../assets/weather/fog.png";
import imgWind from "../../assets/weather/wind.png";

export type WeatherUi = {
  locationName: string;
  temperatureC: number;
  conditionText: string;
  windKmh?: number;
  updatedAt: Date;
  bgImage: string;
};

type Status = "idle" | "loading" | "ready" | "error";

type WeatherCtx = {
  status: Status;
  error: string | null;
  data: WeatherUi | null;
  refresh: () => Promise<void>;
};

const WeatherContext = createContext<WeatherCtx | null>(null);

function roConditionFromOpenMeteo(code: number): string {
  if (code === 0) return "Senin";
  if (code >= 1 && code <= 3) return "Parțial noros / Înnorat";
  if (code === 45 || code === 48) return "Ceață";
  if (code >= 51 && code <= 67) return "Burniță / Ploaie";
  if (code >= 71 && code <= 77) return "Ninsoare";
  if (code >= 80 && code <= 82) return "Averse";
  if (code >= 95 && code <= 99) return "Furtună";
  return "Vreme variabilă";
}

function pickBgImage(code: number, windKmh?: number): string {
  if (code === 45 || code === 48) return imgFog;
  if (code >= 71 && code <= 77) return imgSnow;
  if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) return imgRain;
  if (code >= 1 && code <= 3) return imgCloudy;
  if (windKmh != null && windKmh >= 30) return imgWind;
  return imgClear;
}

// Reverse-geocode by IP
type BdcIpResp = {
  city?: string;
  locality?: string;
  principalSubdivision?: string;
  county?: string;
  region?: string;
  countryName?: string;
  latitude?: number;
  longitude?: number;
};

async function reverseGeocodeByIp(): Promise<{
  locationName: string;
  lat: number;
  lon: number;
}> {
  const { data } = await axios.get<BdcIpResp>(
    "https://api-bdc.io/data/reverse-geocode-client",
    {
      params: { localityLanguage: "ro" },
      timeout: 10_000,
    },
  );

  const lat = data.latitude;
  const lon = data.longitude;
  if (typeof lat !== "number" || typeof lon !== "number") {
    throw new Error("Nu am primit coordonate valide (lat/lon).");
  }

  const city =
    data.city ||
    data.locality ||
    data.principalSubdivision ||
    data.county ||
    data.region ||
    "";
  const country = data.countryName || "";
  const locationName =
    [city, country].filter(Boolean).join(", ") || "Locație necunoscută";

  return { locationName, lat, lon };
}

type OpenMeteoResp = {
  current?: {
    temperature_2m?: number;
    weather_code?: number;
    wind_speed_10m?: number;
  };
};

async function fetchWeather(lat: number, lon: number) {
  const { data } = await axios.get<OpenMeteoResp>(
    "https://api.open-meteo.com/v1/forecast",
    {
      params: {
        latitude: lat,
        longitude: lon,
        current: "temperature_2m,weather_code,wind_speed_10m",
        timezone: "auto",
      },
      timeout: 10_000,
    },
  );

  const temp = data?.current?.temperature_2m;
  const code = data?.current?.weather_code;
  const wind = data?.current?.wind_speed_10m;

  if (typeof temp !== "number" || typeof code !== "number") {
    throw new Error("Weather payload invalid.");
  }

  return {
    temperatureC: temp,
    weatherCode: code,
    windKmh: typeof wind === "number" ? wind : undefined,
  };
}

function preloadImages(urls: string[]) {
  urls.forEach((src) => {
    const img = new Image();
    img.src = src;
  });
}

export function WeatherProvider({ children }: { children: React.ReactNode }) {
  const [status, setStatus] = useState<Status>("idle");
  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<WeatherUi | null>(null);

  const lastFetchRef = useRef<number>(0);
  const CACHE_MS = 2 * 60_000;

  const refresh = async () => {
    setStatus("loading");
    setError(null);

    try {
      const { locationName, lat, lon } = await reverseGeocodeByIp();
      const w = await fetchWeather(lat, lon);

      const conditionText = roConditionFromOpenMeteo(w.weatherCode);
      const bgImage = pickBgImage(w.weatherCode, w.windKmh);

      setData({
        locationName,
        temperatureC: w.temperatureC,
        conditionText,
        windKmh: w.windKmh,
        updatedAt: new Date(),
        bgImage,
      });

      setStatus("ready");
      lastFetchRef.current = Date.now();
    } catch (e: any) {
      setStatus("error");
      setError(e?.message ?? "Eroare la încărcarea vremii.");
    }
  };

  useEffect(() => {
    preloadImages([imgClear, imgCloudy, imgRain, imgSnow, imgFog, imgWind]);

    const now = Date.now();
    if (!data || now - lastFetchRef.current > CACHE_MS) {
      refresh();
    }
  }, []);

  const value = useMemo<WeatherCtx>(
    () => ({
      status,
      error,
      data,
      refresh,
    }),
    [status, error, data, refresh],
  );

  return (
    <WeatherContext.Provider value={value}>{children}</WeatherContext.Provider>
  );
}

export function useWeather() {
  const ctx = useContext(WeatherContext);
  if (!ctx) throw new Error("useWeather must be used inside WeatherProvider");
  return ctx;
}
