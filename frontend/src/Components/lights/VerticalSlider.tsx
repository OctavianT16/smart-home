import React, { useRef, useEffect, useMemo } from "react";
import "./VerticalSlider.css";
import axios from "axios";

type Props = {
  isOn: boolean;
  apiPath: string;
  brightness?: number;
  setBrightness?: (brightness: number) => void;
};

const VerticalSlider = (props: Props) => {
  const { isOn, apiPath, brightness: value, setBrightness: setValue } = props;
  const axios_instance = useMemo(
    () => axios.create({ baseURL: apiPath }),
    [apiPath],
  );

  const debounceRef = useRef<number>(200);
  const sendBrightness = (b: number) => {
    window.clearTimeout(debounceRef.current);
    debounceRef.current = window.setTimeout(() => {
      axios_instance
        .post("", null, { params: { brightness: Math.round(b) } })
        .catch((err) => console.error("Brightness API error", err));
    }, 200);
  };
  const isOnRef = useRef(isOn);

  const sliderRef = useRef<HTMLDivElement | null>(null);

  const draggingRef = useRef<boolean>(false);

  const calcValue = (clientY: number): number => {
    if (!sliderRef.current) return 0;
    const { top, height } = sliderRef.current.getBoundingClientRect();
    const relY = clientY - top;
    const pct = 100 - (relY / height) * 100;
    return Math.max(10, Math.min(100, pct));
  };

  const onMouseDown = (e: React.MouseEvent<HTMLDivElement>) => {
    draggingRef.current = true;
    setValue?.(calcValue(e.clientY));
  };

  const onClick = (e: React.MouseEvent<HTMLDivElement>) => {
    update(e.clientY);
  };

  const update = (clientY: number) => {
    const newVal = calcValue(clientY);
    setValue?.(newVal);
    console.log("isOn: " + isOnRef.current);
    if (isOnRef.current) sendBrightness(newVal);
  };

  const onMouseMove = (e: MouseEvent) => {
    if (draggingRef.current) {
      update(e.clientY);
    }
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
      window.removeEventListener("mousedown", endDrag);
    };
  }, [isOn]);

  return (
    <div className="slider-wrapper">
      <div
        className="slider"
        ref={sliderRef}
        onMouseDown={onMouseDown}
        onClick={onClick}
      >
        <div className="fill" style={{ height: `${value}%` }} />
        <div
          className="handle-line"
          style={{ bottom: `calc(${value}% - 8px)` }}
        />
      </div>
      <div className="value-label" style={{ bottom: `calc(${value}% - 12px)` }}>
        {Math.round(value ? value : 0)}%
      </div>
    </div>
  );
};

export default VerticalSlider;
