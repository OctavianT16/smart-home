import { useEffect, useState } from "react";
import api from "../api";
import {
  FiArrowLeft,
  FiEye,
  FiMoreVertical,
  FiPlus,
  FiTrash2,
} from "react-icons/fi";
import AutomationBuilder from "../Components/automations/AutomationBuilder";
import SceneDetailsModal, {
  type SceneResponse,
} from "../Components/scenes/SceneDetailsModal";

type PageMode = "LIST" | "CREATE";
type AutomationTriggerType = "ARRIVE_HOME" | "LEAVE_HOME" | "TIME_OF_DAY";

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

export default function AutomationsPage() {
  const [mode, setMode] = useState<PageMode>("LIST");
  const [automations, setAutomations] = useState<AutomationResponse[]>([]);
  const [loading, setLoading] = useState(false);

  const [openMenuId, setOpenMenuId] = useState<number | null>(null);
  const [deletingAutomationId, setDeletingAutomationId] = useState<
    number | null
  >(null);
  const [updatingAutomationId, setUpdatingAutomationId] = useState<
    number | null
  >(null);

  const [viewModalOpen, setViewModalOpen] = useState(false);
  const [viewedScene, setViewedScene] = useState<SceneResponse | null>(null);
  const [loadingViewedScene, setLoadingViewedScene] = useState(false);

  useEffect(() => {
    fetchAutomations();
  }, []);

  async function fetchAutomations() {
    try {
      setLoading(true);
      const response = await api.get<AutomationResponse[]>("/api/automations");
      setAutomations(response.data);
    } catch (error) {
      console.error("Eroare la încărcarea automatizărilor:", error);
      alert("Nu s-au putut încărca automatizările.");
    } finally {
      setLoading(false);
    }
  }

  async function toggleAutomation(automation: AutomationResponse) {
    try {
      setUpdatingAutomationId(automation.id);

      await api.patch(`/api/automations/${automation.id}/enabled`, null, {
        params: {
          enabled: !automation.enabled,
        },
      });

      await fetchAutomations();
    } catch (error) {
      console.error("Eroare la actualizarea automatizării:", error);
      alert("Automatizarea nu a putut fi actualizată.");
    } finally {
      setUpdatingAutomationId(null);
    }
  }

  async function deleteAutomation(automationId: number) {
    const confirmed = window.confirm(
      "Sigur vrei să ștergi această automatizare?",
    );

    if (!confirmed) {
      return;
    }

    try {
      setDeletingAutomationId(automationId);
      await api.delete(`/api/automations/${automationId}`);
      await fetchAutomations();
    } catch (error) {
      console.error("Eroare la ștergerea automatizării:", error);
      alert("Automatizarea nu a putut fi ștearsă.");
    } finally {
      setDeletingAutomationId(null);
      setOpenMenuId(null);
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

  function handleAutomationSaved() {
    setMode("LIST");
    fetchAutomations();
  }

  function getTriggerLabel(type: AutomationTriggerType) {
    switch (type) {
      case "ARRIVE_HOME":
        return "Când ajung acasă";
      case "LEAVE_HOME":
        return "Când plec de acasă";
      case "TIME_OF_DAY":
        return "La ora setată";
      default:
        return type;
    }
  }

  function getTriggerDetails(automation: AutomationResponse) {
    if (automation.triggerType === "TIME_OF_DAY") {
      return automation.scheduledTime
        ? `Ora ${automation.scheduledTime.slice(0, 5)}`
        : "Ora nesetată";
    }

    if (automation.triggerType === "ARRIVE_HOME") {
      return "Declanșată de Shortcut la intrarea în zona casei";
    }

    if (automation.triggerType === "LEAVE_HOME") {
      return "Declanșată de Shortcut la ieșirea din zona casei";
    }

    return "-";
  }

  function formatDate(value: string | null) {
    if (!value) {
      return "-";
    }

    return new Date(value).toLocaleString("ro-RO", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  if (mode === "CREATE") {
    return (
      <div className="container py-3">
        <div className="d-flex align-items-center justify-content-between mb-3">
          <div>
            <h3 className="mb-1">Automatizare nouă</h3>
            <p className="text-muted small mb-0">
              Creează o regulă care rulează automat un scenariu.
            </p>
          </div>

          <button
            type="button"
            className="btn btn-outline-secondary btn-sm d-flex align-items-center gap-1"
            onClick={() => setMode("LIST")}
          >
            <FiArrowLeft />
            Înapoi la automatizări
          </button>
        </div>

        <AutomationBuilder onAutomationSaved={handleAutomationSaved} />
      </div>
    );
  }

  return (
    <div className="container py-3">
      <div className="d-flex align-items-center justify-content-between mb-3">
        <div>
          <h3 className="mb-1">Automatizări</h3>
          <p className="text-muted small mb-0">
            Configurează reguli care rulează scene la sosire, plecare sau la o
            oră fixă.
          </p>
        </div>

        <button
          type="button"
          className="btn btn-primary btn-sm d-flex align-items-center gap-1"
          onClick={() => setMode("CREATE")}
        >
          <FiPlus />
          Creează automatizare
        </button>
      </div>

      {loading && (
        <div className="alert alert-light border small">
          Se încarcă automatizările...
        </div>
      )}

      {!loading && automations.length === 0 && (
        <div className="card border-0 shadow-sm">
          <div className="card-body text-center py-5">
            <h5 className="mb-2">Nu ai încă automatizări salvate</h5>
            <p className="text-muted mb-3">
              Creează prima automatizare pentru a rula scene pe baza unui
              trigger.
            </p>
            <button
              type="button"
              className="btn btn-primary btn-sm"
              onClick={() => setMode("CREATE")}
            >
              Creează automatizare
            </button>
          </div>
        </div>
      )}

      {!loading && automations.length > 0 && (
        <div className="row g-3">
          {automations.map((automation) => (
            <div className="col-md-6 col-xl-4" key={automation.id}>
              <div className="card border-0 shadow-sm h-100">
                <div className="card-body">
                  <div className="d-flex justify-content-between align-items-start mb-2">
                    <div>
                      <div className="d-flex align-items-center gap-2">
                        <h5 className="card-title mb-0">{automation.name}</h5>

                        <span
                          className={
                            automation.enabled
                              ? "badge text-bg-success"
                              : "badge text-bg-secondary"
                          }
                        >
                          {automation.enabled ? "Activă" : "Inactivă"}
                        </span>
                      </div>

                      <div className="text-muted small mt-1">
                        Creată la {formatDate(automation.createdAt)}
                      </div>
                    </div>

                    <div className="position-relative">
                      <button
                        type="button"
                        className="btn btn-light btn-sm border-0"
                        onClick={() => {
                          setOpenMenuId(
                            openMenuId === automation.id ? null : automation.id,
                          );
                        }}
                      >
                        <FiMoreVertical />
                      </button>

                      {openMenuId === automation.id && (
                        <div
                          className="position-absolute end-0 mt-1 bg-white border rounded shadow-sm py-1"
                          style={{ zIndex: 20, minWidth: "190px" }}
                        >
                          <button
                            type="button"
                            className="dropdown-item d-flex align-items-center gap-2"
                            onClick={() => openSceneDetails(automation.sceneId)}
                          >
                            <FiEye />
                            Vizualizare scenă
                          </button>

                          <button
                            type="button"
                            className="dropdown-item text-danger d-flex align-items-center gap-2"
                            disabled={deletingAutomationId === automation.id}
                            onClick={() => deleteAutomation(automation.id)}
                          >
                            <FiTrash2 />
                            {deletingAutomationId === automation.id
                              ? "Se șterge..."
                              : "Șterge"}
                          </button>
                        </div>
                      )}
                    </div>
                  </div>

                  <p className="card-text text-muted small mb-3">
                    {automation.description || "Fără descriere."}
                  </p>

                  <div className="mb-3">
                    <div className="text-muted small">Trigger</div>
                    <div className="fw-semibold small">
                      {getTriggerLabel(automation.triggerType)}
                    </div>
                    <div className="text-muted small">
                      {getTriggerDetails(automation)}
                    </div>
                  </div>

                  <div className="mb-3">
                    <div className="text-muted small">Scenariu rulat</div>
                    <div className="fw-semibold small">
                      {automation.sceneName}
                    </div>
                  </div>

                  <div className="mb-3">
                    <div className="text-muted small">Ultima rulare</div>
                    <div className="small">
                      {formatDate(automation.lastTriggeredAt)}
                    </div>
                  </div>

                  <div className="d-flex justify-content-between align-items-center border-top pt-3">
                    <div className="small text-muted">
                      {automation.enabled
                        ? "Automatizarea este activă"
                        : "Automatizarea este oprită"}
                    </div>

                    <div className="form-check form-switch mb-0">
                      <input
                        className="form-check-input"
                        type="checkbox"
                        checked={automation.enabled}
                        disabled={updatingAutomationId === automation.id}
                        onChange={() => toggleAutomation(automation)}
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

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
