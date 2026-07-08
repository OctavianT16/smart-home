import { useWeather } from "./WeatherContext";
import "./WeatherCard.css";
import imgCloudy from "../../assets/weather/cloudy.png";
import { IoReloadCircleOutline } from "react-icons/io5";

export default function WeatherCard() {
  const { status, error, data, refresh } = useWeather();

  return (
    <div className="weather-card card shadow-sm border-0">
      <div className="weather-media">
        <img
          src={data?.bgImage ?? imgCloudy}
          alt="Weather"
          className="weather-media-img"
          draggable={false}
        />

        <div className="weather-overlay" />

        <div className="weather-header">
          <div>
            <div className="weather-location">
              {data?.locationName ?? "Locație..."}
            </div>
            <div className="weather-condition">
              {data?.conditionText ?? "—"}
              {typeof data?.windKmh === "number"
                ? ` • Vânt ${Math.round(data.windKmh)} km/h`
                : ""}
            </div>
          </div>
        </div>

        <div className="weather-footer">
          <div>
            <div className="weather-temp">
              {data ? `${Math.round(data.temperatureC)}°C` : "—"}
            </div>
            <div className="weather-updated">
              {data ? `Actualizat: ${data.updatedAt.toLocaleTimeString()}` : ""}
            </div>
          </div>

          <button
            className="btn fs-3 btn-sm rounded-pill weather-refresh text-white p-2"
            onClick={refresh}
            disabled={status === "loading"}
            title="Refresh"
          >
            <div className="p-0 d-flex justify-content-center">
              <IoReloadCircleOutline />
            </div>
          </button>
        </div>
      </div>

      {status === "error" && (
        <div className="p-3">
          <div className="alert alert-danger mb-0 rounded-4">
            {error ?? "Nu am putut încărca vremea."}
          </div>
        </div>
      )}
    </div>
  );
}
