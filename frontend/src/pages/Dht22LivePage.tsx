import { Dht22LiveCard } from "../Components/telemetry/Dht22LiveCard";
import { Dht22LiveChart } from "../Components/telemetry/Dht22LiveChart";
import { useDht22 } from "../Components/telemetry/Dht22TelemetryContext";
import { AcRemote } from "../Components/airConditioner/AcRemote";
import WeatherCard from "../Components/weather/WeatherCard";

export const Dht22LivePage: React.FC = () => {
  const backendBaseUrl = "";
  const {
    series,
    connectionState,
    lastError,
    windowMinutes,
    setWindowMinutes,
    lastMessage,
    clearSeries,
  } = useDht22();

  return (
    <div className="container">
      <div className="row mb-3">
        <div>
          <h3 className="m-0">DHT22 Realtime</h3>
          <div className="text-muted small">
            Status: <span className="fw-semibold">{connectionState}</span>
            {lastError ? (
              <>
                {" "}
                · <span className="text-danger">{lastError}</span>
              </>
            ) : null}
          </div>
        </div>
      </div>
      <div className="row gy-3 justify-content-center gap-4  ">
        <div
          className="row gy-3 gx-0 d-flex justify-content-evenly m-0"
          style={{ width: "85%" }}
        >
          <div className="col-12 col-lg-5 d-flex flex-column gap-3">
            <WeatherCard />

            <Dht22LiveCard
              deviceId="esp32-living"
              backendBaseUrl={backendBaseUrl || undefined}
              lastMessage={lastMessage}
            />
          </div>

          <div className="col-12 col-lg-4 px-0">
            <AcRemote />
          </div>
        </div>

        <div
          className="row g-0 d-flex flex-row-reverse mb-0 justify-content-evenly"
          style={{ width: "85%" }}
        >
          <div className="d-flex gap-2 col-6 col-lg-5 justify-content-end">
            <button
              className="btn btn-outline-secondary btn-sm"
              onClick={clearSeries}
            >
              Clear
            </button>

            <div
              className="btn-group btn-group-sm"
              role="group"
              aria-label="Window"
            >
              <button
                className={`btn btn-outline-primary ${
                  windowMinutes === 60 ? "active" : ""
                }`}
                onClick={() => setWindowMinutes(60)}
              >
                1h
              </button>
              <button
                className={`btn btn-outline-primary ${
                  windowMinutes === 180 ? "active" : ""
                }`}
                onClick={() => setWindowMinutes(180)}
              >
                3h
              </button>
              <button
                className={`btn btn-outline-primary ${
                  windowMinutes === 1440 ? "active" : ""
                }`}
                onClick={() => setWindowMinutes(1440)}
              >
                24h
              </button>
            </div>
          </div>
          <div className="text-muted small mt-2 col-6 col-lg-4"></div>
        </div>
        <div
          className="row g-0 d-flex justify-content-evenly mb-4 mt-0"
          style={{ width: "85%" }}
        >
          <div className="col-12 col-lg-10 mt-0">
            <Dht22LiveChart data={series} windowMinutes={windowMinutes} />
          </div>
        </div>
      </div>
    </div>
    //{" "}
  );
};
