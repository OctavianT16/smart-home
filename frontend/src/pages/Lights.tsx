import { useEffect, useState } from "react";
import LightCard from "../Components/lights/LightCard";
import VerticalKelvinSlider from "../Components/lights/VerticalKelvinSlider";
import VerticalSlider from "../Components/lights/VerticalSlider";
import ModeGrid from "../Components/lights/ModeGrid";
import api from "../api";

type WizPilotState = {
  state: boolean;
  dimming: number;
  sceneId: string;
};

function Lights() {
  const [smartBulbOn, setSmartBulbOn] = useState<boolean>(false);
  const [isOn, setIsOn] = useState(false);
  const [selectedMode, setSelectedMode] = useState<string | null>(null);
  const [brightWizValue, setBrightWizValue] = useState<number>(20);
  const [espbrightValue, setEspBrightValue] = useState<number>(20);

  const fetchWizState = async () => {
    try {
      const response = await api.get<WizPilotState>("/api/light/pilot");

      setSmartBulbOn(response.data.state === true);
      setBrightWizValue(response.data.dimming);
      setSelectedMode(response.data.sceneId ?? null);
    } catch (error) {
      console.error("Nu s-a putut obține starea becului WiZ:", error);
    }
  };

  useEffect(() => {
    fetchWizState();
  }, []);

  return (
    <>
      <h2 className="mb-4">Control iluminat</h2>
      <div className="container mt-5">
        <div className="row gap-4">
          <div className="col-md-6 p-2 ">
            <LightCard
              isOn={smartBulbOn}
              setIsOn={setSmartBulbOn}
              label="Philips WiZ"
              apiPath="/api/light"
            ></LightCard>
            <div className="d-flex flex-row w-100 my-3">
              <div className="w-50 d-flex justify-content-center align-items-center">
                <div className="d-inline-block me-5">
                  <VerticalSlider
                    isOn={smartBulbOn}
                    apiPath="/api/light/brightness"
                    brightness={brightWizValue}
                    setBrightness={setBrightWizValue}
                  />
                </div>
                <div className="d-inline-block">
                  <VerticalKelvinSlider
                    isOn={smartBulbOn}
                    selectedMode={selectedMode}
                    setSelectedMode={setSelectedMode}
                  />
                </div>
              </div>
              <section className="w-50 d-flex flex-column justify-content-center align-items-center">
                <div className="d-flex flex-column" style={{ width: "85%" }}>
                  <ModeGrid
                    selectedMode={selectedMode}
                    setSelectedMode={setSelectedMode}
                    setisOn={setSmartBulbOn}
                  />
                </div>
              </section>
            </div>
          </div>
          <div className="col-md-4 p-2">
            <LightCard
              isOn={isOn}
              setIsOn={setIsOn}
              label="Esp32 - Flower"
              apiPath="/api/flower/power"
            ></LightCard>
            <div className="d-flex flex-row w-100 my-3">
              <div className="w-50 d-flex flex-column justify-content-center align-items-center">
                <div className="d-inline-block">
                  <VerticalSlider
                    isOn={isOn}
                    apiPath="/api/flower/brightness"
                    brightness={espbrightValue}
                    setBrightness={setEspBrightValue}
                  />
                </div>
                <h6>Control PWM</h6>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default Lights;
