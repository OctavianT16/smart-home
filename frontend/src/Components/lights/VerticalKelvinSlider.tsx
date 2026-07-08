import React, { useState, useRef, useEffect } from "react";
import "./VerticalKelvinSlider.css";
import axios from "axios";
const MIN_KELVIN = 2200;
const MAX_KELVIN = 6200;

const VerticalKelvinSlider = ({
  isOn,
  setSelectedMode,
  selectedMode,
}: {
  isOn: boolean;
  selectedMode: string | null;
  setSelectedMode: (mode: string | null) => void;
}) => {
  const [pct, setPct] = useState<number>(50);
  const sliderRef = useRef<HTMLDivElement | null>(null);
  const draggingRef = useRef<boolean>(false);
  const debounceRef = useRef<number>(0);
  const isOnRef = useRef(isOn);
  const axios_instance = axios.create({
    baseURL: "/api/light/temp",
  });

  const pctToKelvin = (v: number): number =>
    Math.round(MIN_KELVIN + ((MAX_KELVIN - MIN_KELVIN) * v) / 100);

  const sendTemperature = (kelvin: number) => {
    window.clearTimeout(debounceRef.current);
    debounceRef.current = window.setTimeout(() => {
      axios_instance
        .post("", null, { params: { temp: kelvin } })
        .catch((err) => console.error("Temperature API error", err));
    }, 200);
  };

  const calcPct = (clientY: number): number => {
    if (!sliderRef.current) return 0;
    const { top, height } = sliderRef.current.getBoundingClientRect();
    const relY = clientY - top;
    const rawPct = 100 - (relY / height) * 100;
    return Math.max(0, Math.min(100, rawPct));
  };

  const onMouseDown = (e: React.MouseEvent) => {
    draggingRef.current = true;
    if (selectedMode) setSelectedMode(null);
    setPct(calcPct(e.clientY));
  };

  const onMouseMove = (e: MouseEvent) => {
    if (draggingRef.current) update(e.clientY);
  };

  const update = (clientY: number) => {
    const newPct = calcPct(clientY);
    setPct(newPct);
    const kelvin = pctToKelvin(newPct);
    if (isOnRef.current) sendTemperature(kelvin);
  };

  const onClick = (e: React.MouseEvent<HTMLDivElement>) => {
    update(e.clientY);
    setSelectedMode(null);
  };

  const endDrag = () => {
    draggingRef.current = false;
  };

  useEffect(() => {
    isOnRef.current = isOn;
    window.addEventListener("mousemove", onMouseMove);
    window.addEventListener("mouseup", endDrag);
    return () => {
      window.removeEventListener("mousemove", onMouseMove);
      window.removeEventListener("mouseup", endDrag);
      window.removeEventListener("touchend", endDrag);
    };
  }, [isOn]);

  const kelvin = pctToKelvin(pct);

  return (
    <div className="kelvin-slider-wrapper">
      <div
        className="kelvin-slider"
        ref={sliderRef}
        onMouseDown={onMouseDown}
        onClick={onClick}
      >
        <div className="kelvin-little">
          <div
            className="handle-line"
            style={{ bottom: `calc(${pct}% - 10px)` }}
          >
            <div></div>
          </div>
        </div>
      </div>
      <div className="kelvin-little">
        <div
          className="kelvin-label"
          style={{ bottom: `calc(${pct}% - 15px)` }}
        >
          {kelvin} K
        </div>
      </div>
    </div>
  );
};

export default VerticalKelvinSlider;
