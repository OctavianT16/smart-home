import { useEffect, useState } from "react";
import { FiEye, FiMoreVertical } from "react-icons/fi";
import SceneDetailsModal, {
  type SceneResponse,
} from "../scenes/SceneDetailsModal";
import api from "../../api";

type AutomationTriggerType = "ARRIVE_HOME" | "LEAVE_HOME" | "TIME_OF_DAY";

interface SceneSummaryResponse {
  id: number;
  name: string;
  description: string;
  enabled: boolean;
  createdAt: string;
  actionCount: number;
  deviceCount: number;
}

interface AutomationResponse {
  id: number;
  name: string;
  description: string;
  triggerType: AutomationTriggerType;
  scheduledTime: string | null;
  enabled: boolean;
  createdAt: string;
  lastTriggeredAt: string | null;
  sceneId: number;
  sceneName: string;
}

interface CreateAutomationRequest {
  name: string;
  description: string;
  triggerType: AutomationTriggerType;
  scheduledTime: string | null;
  enabled: boolean;
  sceneId: number;
}

interface AutomationBuilderProps {
  onAutomationSaved?: (automation: AutomationResponse) => void;
}

export default function AutomationBuilder({
  onAutomationSaved,
}: AutomationBuilderProps) {
  const [scenes, setScenes] = useState<SceneSummaryResponse[]>([]);
  const [selectedSceneId, setSelectedSceneId] = useState<number | null>(null);

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [triggerType, setTriggerType] =
    useState<AutomationTriggerType>("ARRIVE_HOME");
  const [scheduledTime, setScheduledTime] = useState("08:00");
  const [enabled, setEnabled] = useState(true);

  const [loadingScenes, setLoadingScenes] = useState(false);
  const [saving, setSaving] = useState(false);

  const [openMenuId, setOpenMenuId] = useState<number | null>(null);
  const [viewModalOpen, setViewModalOpen] = useState(false);
  const [viewedScene, setViewedScene] = useState<SceneResponse | null>(null);
  const [loadingViewedScene, setLoadingViewedScene] = useState(false);

  useEffect(() => {
    fetchScenes();
  }, []);

  async function fetchScenes() {
    try {
      setLoadingScenes(true);
      const response = await api.get<SceneSummaryResponse[]>("/api/scenes");
      setScenes(response.data);
    } catch (error) {
      console.error("Eroare la încărcarea scenariilor:", error);
      alert("Nu s-au putut încărca scenariile.");
    } finally {
      setLoadingScenes(false);
    }
  }

  async function openSceneDetails(sceneId: number) {
    try {
      setViewModalOpen(true);
      setLoadingViewedScene(true);
      setViewedScene(null);
      setOpenMenuId(null);

      const response = await api.get<SceneResponse>(`/api/scenes/${sceneId}`);

      setViewedScene(response.data);
    } catch (error) {
      console.error("Eroare la încărcarea scenariului:", error);
      alert("Scenariul nu a putut fi încărcat.");
      setViewModalOpen(false);
    } finally {
      setLoadingViewedScene(false);
    }
  }

  async function handleSaveAutomation() {
    if (!name.trim()) {
      alert("Introdu un nume pentru automatizare.");
      return;
    }

    if (!selectedSceneId) {
      alert("Alege scenariul care trebuie rulat.");
      return;
    }

    if (triggerType === "TIME_OF_DAY" && !scheduledTime) {
      alert("Alege ora pentru automatizare.");
      return;
    }

    const payload: CreateAutomationRequest = {
      name: name.trim(),
      description: description.trim(),
      triggerType,
      scheduledTime: triggerType === "TIME_OF_DAY" ? scheduledTime : null,
      enabled,
      sceneId: selectedSceneId,
    };

    try {
      setSaving(true);

      const response = await api.post<AutomationResponse>(
        "/api/automations",
        payload,
      );

      alert(`Automatizarea "${response.data.name}" a fost salvată.`);
      onAutomationSaved?.(response.data);
    } catch (error) {
      console.error("Eroare la salvarea automatizării:", error);
      alert("Automatizarea nu a putut fi salvată.");
    } finally {
      setSaving(false);
    }
  }

  function getTriggerLabel(type: AutomationTriggerType) {
    switch (type) {
      case "ARRIVE_HOME":
        return "Când ajung acasă";
      case "LEAVE_HOME":
        return "Când plec de acasă";
      case "TIME_OF_DAY":
        return "La o anumită oră";
      default:
        return type;
    }
  }

  const selectedScene = scenes.find((scene) => scene.id === selectedSceneId);

  return (
    <div className="container py-3">
      <div className="mb-3">
        <h3 className="mb-1">Creare automatizare</h3>
        <p className="text-muted small mb-0">
          Alege un trigger și scenariul care va fi rulat automat.
        </p>
      </div>

      <div className="card border-0 shadow-sm mb-3">
        <div className="card-body py-3">
          <div className="row g-3">
            <div className="col-md-4">
              <label className="form-label small fw-semibold">
                Nume automatizare
              </label>
              <input
                type="text"
                className="form-control form-control-sm"
                value={name}
                onChange={(event) => setName(event.target.value)}
                placeholder="Ex: Plecare de acasă"
              />
            </div>

            <div className="col-md-8">
              <label className="form-label small fw-semibold">Descriere</label>
              <input
                type="text"
                className="form-control form-control-sm"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                placeholder="Ex: Rulează scena de oprire a dispozitivelor"
              />
            </div>

            <div className="col-md-4">
              <label className="form-label small fw-semibold">Trigger</label>
              <select
                className="form-select form-select-sm"
                value={triggerType}
                onChange={(event) =>
                  setTriggerType(event.target.value as AutomationTriggerType)
                }
              >
                <option value="ARRIVE_HOME">Când ajung acasă</option>
                <option value="LEAVE_HOME">Când plec de acasă</option>
                <option value="TIME_OF_DAY">La o anumită oră</option>
              </select>
            </div>

            {triggerType === "TIME_OF_DAY" && (
              <div className="col-md-3">
                <label className="form-label small fw-semibold">Ora</label>
                <input
                  type="time"
                  className="form-control form-control-sm"
                  value={scheduledTime}
                  onChange={(event) => setScheduledTime(event.target.value)}
                />
              </div>
            )}

            <div className="col-md-3 d-flex align-items-end">
              <div className="form-check form-switch">
                <input
                  className="form-check-input"
                  type="checkbox"
                  checked={enabled}
                  onChange={(event) => setEnabled(event.target.checked)}
                  id="automationEnabled"
                />
                <label
                  className="form-check-label small"
                  htmlFor="automationEnabled"
                >
                  Automatizare activă
                </label>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="card border-0 shadow-sm mb-3">
        <div className="card-header bg-white py-2">
          <div className="fw-semibold">Alege scenariul rulat</div>
          <div className="text-muted small">
            Automatizarea va executa scenariul selectat când trigger-ul devine
            activ.
          </div>
        </div>

        <div className="card-body">
          {loadingScenes && (
            <div className="alert alert-light border small mb-0">
              Se încarcă scenariile...
            </div>
          )}

          {!loadingScenes && scenes.length === 0 && (
            <div className="text-muted small">
              Nu există scenarii salvate. Creează mai întâi un scenariu.
            </div>
          )}

          {!loadingScenes && scenes.length > 0 && (
            <div className="row g-3">
              {scenes.map((scene) => (
                <div className="col-md-6 col-xl-4" key={scene.id}>
                  <div
                    className={
                      selectedSceneId === scene.id
                        ? "card h-100 border-primary shadow-sm"
                        : "card h-100 border-0 shadow-sm"
                    }
                    role="button"
                    style={{ cursor: "pointer" }}
                    onClick={() => setSelectedSceneId(scene.id)}
                  >
                    <div className="card-body">
                      <div className="d-flex justify-content-between align-items-start mb-2">
                        <h6 className="mb-0">{scene.name}</h6>

                        <div className="position-relative">
                          <button
                            type="button"
                            className="btn btn-light btn-sm border-0"
                            onClick={(event) => {
                              event.stopPropagation();
                              setOpenMenuId(
                                openMenuId === scene.id ? null : scene.id,
                              );
                            }}
                          >
                            <FiMoreVertical />
                          </button>

                          {openMenuId === scene.id && (
                            <div
                              className="position-absolute end-0 mt-1 bg-white border rounded shadow-sm py-1"
                              style={{ zIndex: 20, minWidth: "160px" }}
                              onClick={(event) => event.stopPropagation()}
                            >
                              <button
                                type="button"
                                className="dropdown-item d-flex align-items-center gap-2"
                                onClick={() => openSceneDetails(scene.id)}
                              >
                                <FiEye />
                                Vizualizare
                              </button>
                            </div>
                          )}
                        </div>
                      </div>

                      <p className="text-muted small mb-3">
                        {scene.description || "Fără descriere."}
                      </p>

                      <div className="d-flex gap-2 flex-wrap">
                        <span className="badge text-bg-light border">
                          {scene.actionCount} acțiuni
                        </span>
                        <span className="badge text-bg-light border">
                          {scene.deviceCount} dispozitive
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {selectedScene && (
            <div className="alert alert-light border small mt-3 mb-0">
              Scenariu selectat: <strong>{selectedScene.name}</strong>
            </div>
          )}
        </div>
      </div>

      <div className="card border-0 shadow-sm">
        <div className="card-body d-flex justify-content-between align-items-center py-3">
          <div className="small text-muted">
            Trigger selectat: <strong>{getTriggerLabel(triggerType)}</strong>
            {triggerType === "TIME_OF_DAY" && (
              <>
                {" "}
                · Ora: <strong>{scheduledTime}</strong>
              </>
            )}
          </div>

          <button
            type="button"
            className="btn btn-success btn-sm"
            onClick={handleSaveAutomation}
            disabled={saving}
          >
            {saving ? "Se salvează..." : "Salvează automatizarea"}
          </button>
        </div>
      </div>

      {viewModalOpen && (
        <SceneDetailsModal
          scene={viewedScene}
          loading={loadingViewedScene}
          onClose={() => {
            setViewModalOpen(false);
            setViewedScene(null);
          }}
        />
      )}
    </div>
  );
}
