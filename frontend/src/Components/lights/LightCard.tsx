import React, { useMemo } from "react";
import axios from "axios";
import "./LightCard.css";
import { HiOutlineLightBulb } from "react-icons/hi";
import { PiFlowerTulipDuotone } from "react-icons/pi";
interface LightCardProps {
  label: string;
  ip: string;
  setIsOn: (isOn: boolean) => void;
  isOn: boolean;
  apiPath: string;
}

const LightCard = ({
  label,
  isOn,
  setIsOn,
  apiPath,
}: Partial<LightCardProps>) => {
  const axios_instance = useMemo(
    () => axios.create({ baseURL: apiPath }),
    [apiPath],
  );
  const toggleLight = async () => {
    const toggle = isOn ? "/off" : "/on";
    if (label == "Philips WiZ") {
      try {
        await axios_instance.post(`${toggle}`);
        setIsOn!(!isOn);
      } catch (error) {
        console.error("Failed to toggle light:", error);
      }
    } else
      try {
        await axios_instance.post("", null, { params: { on: !isOn } });
        setIsOn!(!isOn);
      } catch (error) {
        console.error("Failed to toggle light:", error);
      }
  };

  const iconWrapperStyle: React.CSSProperties = {
    width: "55px",
    height: "55px",
    borderRadius: "50%",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: isOn
      ? label === "Philips WiZ"
        ? "linear-gradient(135deg,rgb(242, 255, 102),rgb(255, 77, 0))"
        : "linear-gradient(135deg,rgb(31, 237, 185),rgba(30, 231, 63, 0.47))"
      : "#e0e0e0",
    color: isOn ? "#fff8dc" : "#888",
    fontSize: "30px",
    transition: "all 0.4s ease",
  };

  return (
    <div
      className={`card p-3 light-card ${
        isOn
          ? label === "Philips WiZ"
            ? "bg-purple text-white"
            : "bg-pink text-white"
          : "back-white text-dark"
      }`}
    >
      <div className="d-flex justify-content-between align-items-center">
        <span className="fw-bold">{isOn ? "ON" : "OFF"}</span>
        <div className="form-check form-switch">
          <input
            className="form-check-input"
            type="checkbox"
            role="switch"
            checked={isOn}
            onChange={toggleLight}
          />
        </div>
      </div>
      <div className="mt-3 text-start">
        <div className="ms-0" style={iconWrapperStyle}>
          {label === "Philips WiZ" ? (
            <HiOutlineLightBulb />
          ) : (
            <PiFlowerTulipDuotone />
          )}
        </div>
        <div className="mt-2 fw-bold">{label}</div>
      </div>
    </div>
  );
};

export default LightCard;
