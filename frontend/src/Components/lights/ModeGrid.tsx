import { useState } from "react";
import api from "../../api.ts";
import "./ModeGrid.css";

const modes = [
  { id: "tv_time", label: "TV Time", src: "src/assets/tv-time.png" },
  { id: "jungle", label: "Jungle", src: "src/assets/jungle.png" },
  { id: "summer", label: "Summer", src: "src/assets/summer.png" },
  { id: "relax", label: "Relax", src: "src/assets/relax.png" },
];

interface ModeGridProps {
  selectedMode: string | null;
  setSelectedMode: (selectedMode: string | null) => void;
  setisOn: (isOn: boolean) => void;
}

const ModeGrid = ({
  selectedMode,
  setSelectedMode,
  setisOn,
}: ModeGridProps) => {
  const [loadingId, setLoadingId] = useState<string | null>(null);

  const handleSelect = async (modeId: string) => {
    if (loadingId) return;
    const prev = selectedMode;

    setSelectedMode(modeId);
    setLoadingId(modeId);

    try {
      await api.post("/light/mode", null, {
        params: { mode: modeId },
        baseURL: "/api",
      });
      setisOn(true);
    } catch (err) {
      console.error("Failed to set mode:", err);
      setSelectedMode(prev ?? null);
    } finally {
      setLoadingId(null);
    }
  };

  return (
    <div className="mode-grid">
      {modes.map((mode) => (
        <div
          key={mode.id}
          className={`mode-item ${selectedMode === mode.id ? "selected" : ""} ${
            loadingId === mode.id ? "is-loading" : ""
          }`}
          onClick={() => handleSelect(mode.id)}
          role="button"
          aria-pressed={selectedMode === mode.id}
        >
          <img src={mode.src} alt={mode.label} className="mode-icon" />
          <span className="mode-label">{mode.label}</span>
        </div>
      ))}
    </div>
  );
};

export default ModeGrid;
