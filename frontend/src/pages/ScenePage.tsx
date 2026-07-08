import { useEffect, useState } from "react";
import api from "../api";
import { useAuth } from "../Components/auth/AuthContext";
import {
  FiEye,
  FiMoreVertical,
  FiPlus,
  FiPlay,
  FiTrash2,
  FiArrowLeft,
} from "react-icons/fi";
import SceneBuilder from "../Components/scenes/SceneBuilder";
import SceneDetailsModal from "../Components/scenes/SceneDetailsModal";

interface SceneSummaryResponse {
  id: number;
  name: string;
  description: string;
  enabled: boolean;
  createdAt: string;
  actionCount: number;
  deviceCount: number;
}

interface SceneActionResponse {
  id: number;
  executionOrder: number;
  delayMs: number;
  deviceId: number;
  deviceName: string;
  capability: string;
  command: string;
  parameters: Record<string, string | number | boolean>;
}

interface SceneResponse {
  id: number;
  name: string;
  description: string;
  enabled: boolean;
  createdAt: string;
  actions: SceneActionResponse[];
}

type PageMode = "LIST" | "CREATE";

export default function ScenesPage() {
  const [mode, setMode] = useState<PageMode>("LIST");
  const [scenes, setScenes] = useState<SceneSummaryResponse[]>([]);
  const [loading, setLoading] = useState(false);

  const [openMenuId, setOpenMenuId] = useState<number | null>(null);

  const [viewModalOpen, setViewModalOpen] = useState(false);
  const [viewedScene, setViewedScene] = useState<SceneResponse | null>(null);
  const [loadingViewedScene, setLoadingViewedScene] = useState(false);

  const [runningSceneId, setRunningSceneId] = useState<number | null>(null);
  const [deletingSceneId, setDeletingSceneId] = useState<number | null>(null);

  const { isAdmin } = useAuth();

  useEffect(() => {
    fetchScenes();
  }, []);

  const fetchScenes = async () => {
    try {
      setLoading(true);
      const response = await api.get<SceneSummaryResponse[]>("/api/scenes");
      setScenes(response.data);
    } catch (error) {
      console.error("Eroare la încărcarea scenariilor:", error);
      alert("Nu s-au putut încărca scenariile.");
    } finally {
      setLoading(false);
    }
  };

  async function runScene(sceneId: number) {
    try {
      setRunningSceneId(sceneId);
      await api.post(`/api/scenes/${sceneId}/run`);
    } catch (error) {
      console.error("Eroare la rularea scenariului:", error);
      alert("Scenariul nu a putut fi rulat.");
    } finally {
      setRunningSceneId(null);
    }
  }

  async function deleteScene(sceneId: number) {
    const confirmed = window.confirm("Sigur vrei să ștergi acest scenariu?");

    if (!confirmed) {
      return;
    }

    try {
      setDeletingSceneId(sceneId);
      await api.delete(`/api/scenes/${sceneId}`);
      await fetchScenes();
    } catch (error) {
      console.error("Eroare la ștergerea scenariului:", error);
      alert("Scenariul nu a putut fi șters.");
    } finally {
      setDeletingSceneId(null);
      setOpenMenuId(null);
    }
  }

  async function openViewModal(sceneId: number) {
    try {
      setViewModalOpen(true);
      setLoadingViewedScene(true);
      setViewedScene(null);

      const response = await api.get<SceneResponse>(`/api/scenes/${sceneId}`);

      setViewedScene(response.data);
    } catch (error) {
      console.error("Eroare la încărcarea detaliilor scenariului:", error);
      alert("Detaliile scenariului nu au putut fi încărcate.");
      setViewModalOpen(false);
    } finally {
      setLoadingViewedScene(false);
      setOpenMenuId(null);
    }
  }

  function handleSceneSaved() {
    setMode("LIST");
    fetchScenes();
  }

  function formatDate(value: string) {
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
            <h3 className="mb-1">Scenariu nou</h3>
            <p className="text-muted small mb-0">
              Creează un scenariu nou și adaugă acțiunile dorite.
            </p>
          </div>

          <button
            type="button"
            className="btn btn-outline-secondary btn-sm d-flex align-items-center gap-1"
            onClick={() => setMode("LIST")}
          >
            <FiArrowLeft />
            Înapoi la scene
          </button>
        </div>

        <SceneBuilder onSceneSaved={handleSceneSaved} />
      </div>
    );
  }

  return (
    <div className="container py-3">
      <div className="d-flex align-items-center justify-content-between mb-3">
        <div>
          <h3 className="mb-1">Scene</h3>
          <p className="text-muted small mb-0">
            Rulează rapid scenarii salvate sau creează unele noi.
          </p>
        </div>

        {isAdmin && (
          <button
            type="button"
            className="btn btn-primary btn-sm d-flex align-items-center gap-1"
            onClick={() => setMode("CREATE")}
          >
            <FiPlus />
            Creează scenariu
          </button>
        )}
      </div>

      {loading && (
        <div className="alert alert-light border small">
          Se încarcă scenariile...
        </div>
      )}

      {!loading && scenes.length === 0 && (
        <div className="card border-0 shadow-sm">
          <div className="card-body text-center py-5">
            <h5 className="mb-2">Nu ai încă scenarii salvate</h5>
            <p className="text-muted mb-3">
              Creează primul scenariu pentru a grupa mai multe acțiuni smart
              home.
            </p>
            <button
              type="button"
              className="btn btn-primary btn-sm"
              onClick={() => setMode("CREATE")}
            >
              Creează scenariu
            </button>
          </div>
        </div>
      )}

      {!loading && scenes.length > 0 && (
        <div className="row g-3">
          {scenes
            .filter((scene) => scene.enabled)
            .map((scene) => (
              <div className="col-md-6 col-xl-4" key={scene.id}>
                <div
                  className="card border-0 shadow-sm h-100 scene-card"
                  role="button"
                  onClick={() => runScene(scene.id)}
                  style={{ cursor: "pointer" }}
                >
                  <div className="card-body">
                    <div className="d-flex justify-content-between align-items-start mb-2">
                      <div>
                        <div className="d-flex align-items-center gap-2">
                          <h5 className="card-title mb-0">{scene.name}</h5>

                          {runningSceneId === scene.id && (
                            <span className="badge text-bg-primary">
                              Rulează...
                            </span>
                          )}
                        </div>

                        <div className="text-muted small mt-1">
                          Creat la {formatDate(scene.createdAt)}
                        </div>
                      </div>

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
                            style={{ zIndex: 20, minWidth: "170px" }}
                            onClick={(event) => event.stopPropagation()}
                          >
                            <button
                              type="button"
                              className="dropdown-item d-flex align-items-center gap-2"
                              onClick={() => openViewModal(scene.id)}
                            >
                              <FiEye />
                              Vizualizare
                            </button>

                            {isAdmin && (
                              <button
                                type="button"
                                className="dropdown-item text-danger d-flex align-items-center gap-2"
                                disabled={deletingSceneId === scene.id}
                                onClick={() => deleteScene(scene.id)}
                              >
                                <FiTrash2 />
                                {deletingSceneId === scene.id
                                  ? "Se șterge..."
                                  : "Șterge"}
                              </button>
                            )}
                          </div>
                        )}
                      </div>
                    </div>

                    <p className="card-text text-muted small mb-3">
                      {scene.description || "Fără descriere."}
                    </p>

                    <div className="d-flex gap-2 flex-wrap mb-3">
                      <span className="badge text-bg-light border">
                        {scene.actionCount} acțiuni
                      </span>

                      <span className="badge text-bg-light border">
                        {scene.deviceCount} dispozitive
                      </span>

                      <span className="badge text-bg-light border">
                        {scene.enabled ? "Activ" : "Dezactivat"}
                      </span>
                    </div>

                    <div className="small text-muted d-flex align-items-center gap-1">
                      <FiPlay />
                      Click pe card pentru rulare
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
          runningSceneId={runningSceneId}
          onRunScene={() => {
            viewedScene && runScene(viewedScene.id);
          }}
        />
      )}
    </div>
  );
}
