import { NavLink, Outlet } from "react-router-dom";
import { Dht22TelemetryProvider } from "./Components/telemetry/Dht22TelemetryContext";
import { WeatherProvider } from "./Components/weather/WeatherContext";
import { useNavigate } from "react-router-dom";
import { useAuth } from "./Components/auth/AuthContext";
import "./Layout.css";

export default function Layout() {
  const backendBaseUrl = "";
  const deviceId = "esp32-living";

  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    try {
      await logout();
      navigate("/login");
    } catch (error) {
      console.error("Logout error:", error);
      navigate("/login");
    }
  }

  return (
    <Dht22TelemetryProvider backendBaseUrl={backendBaseUrl} deviceId={deviceId}>
      <WeatherProvider>
        <div className="app-shell">
          <aside className="app-sidebar">
            <div className="mt-2 px-2">
              {user && (
                <div className="mb-2 small text-muted text-center">
                  {user.username} ({user.role})
                </div>
              )}

              <button
                type="button"
                className="btn btn-outline-danger btn-sm w-100"
                onClick={handleLogout}
              >
                Logout
              </button>
            </div>

            <NavLink
              to="/"
              end
              className={({ isActive }) =>
                `sidebar-btn ${isActive ? "active" : ""}`
              }
            >
              Climate
            </NavLink>
            <NavLink
              to="/dehumidifier"
              end
              className={({ isActive }) =>
                `sidebar-btn ${isActive ? "active" : ""}`
              }
            >
              Dehumidifier
            </NavLink>
            <NavLink
              to="/lights"
              className={({ isActive }) =>
                `sidebar-btn ${isActive ? "active" : ""}`
              }
            >
              Lights
            </NavLink>

            <NavLink
              to="/tapo"
              className={({ isActive }) =>
                `sidebar-btn ${isActive ? "active" : ""}`
              }
            >
              Tapo
            </NavLink>

            <NavLink
              to="/scenes"
              className={({ isActive }) =>
                `sidebar-btn ${isActive ? "active" : ""}`
              }
            >
              Scenes
            </NavLink>

            <NavLink
              to="/automation"
              className={({ isActive }) =>
                `sidebar-btn ${isActive ? "active" : ""}`
              }
            >
              Automations
            </NavLink>
          </aside>

          <main className="app-main">
            <Outlet />
          </main>
        </div>
      </WeatherProvider>
    </Dht22TelemetryProvider>
  );
}
