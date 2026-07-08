import { FiPlay } from "react-icons/fi";

export interface SceneActionResponse {
  id: number;
  executionOrder: number;
  delayMs: number;
  deviceId: number;
  deviceName: string;
  capability: string;
  command: string;
  parameters: Record<string, string | number | boolean>;
}

export interface SceneResponse {
  id: number;
  name: string;
  description: string;
  enabled: boolean;
  createdAt: string;
  actions: SceneActionResponse[];
}

interface SceneDetailsModalProps {
  scene: SceneResponse | null;
  loading: boolean;
  runningSceneId?: number | null;
  onClose: () => void;
  onRunScene?: (sceneId: number) => void;
}

export default function SceneDetailsModal({
  scene,
  loading,
  runningSceneId,
  onClose,
  onRunScene,
}: SceneDetailsModalProps) {
  function formatDelay(delayMs: number) {
    const seconds = delayMs / 1000;

    if (Number.isInteger(seconds)) {
      return `${seconds}s`;
    }

    return `${seconds.toFixed(2)}s`;
  }

  function formatParameters(
    parameters: Record<string, string | number | boolean>,
  ) {
    const entries = Object.entries(parameters);

    if (entries.length === 0) {
      return "-";
    }

    return entries.map(([key, value]) => `${key}: ${value}`).join(", ");
  }

  return (
    <>
      <div className="modal show d-block" tabIndex={-1}>
        <div className="modal-dialog modal-xl modal-dialog-centered">
          <div className="modal-content">
            <div className="modal-header py-2">
              <div>
                <h5 className="modal-title mb-0">
                  {scene?.name ?? "Detalii scenariu"}
                </h5>
                <small className="text-muted">
                  Acțiunile sunt afișate în ordinea execuției.
                </small>
              </div>

              <button type="button" className="btn-close" onClick={onClose} />
            </div>

            <div className="modal-body">
              {loading && (
                <div className="alert alert-light border small mb-0">
                  Se încarcă detaliile scenariului...
                </div>
              )}

              {!loading && scene && (
                <>
                  <div className="mb-3">
                    <div className="text-muted small">Descriere</div>
                    <div>{scene.description || "Fără descriere."}</div>
                  </div>

                  <div className="table-responsive">
                    <table className="table table-sm align-middle">
                      <thead className="table-light">
                        <tr>
                          <th style={{ width: "60px" }}>#</th>
                          <th>Dispozitiv</th>
                          <th>Capabilitate</th>
                          <th>Comandă</th>
                          <th>Parametri</th>
                          <th>Delay</th>
                        </tr>
                      </thead>

                      <tbody>
                        {scene.actions.map((action) => (
                          <tr key={action.id}>
                            <td>{action.executionOrder}</td>
                            <td className="fw-semibold small">
                              {action.deviceName}
                            </td>
                            <td>
                              <span className="badge text-bg-light border">
                                {action.capability}
                              </span>
                            </td>
                            <td className="small">{action.command}</td>
                            <td className="small text-muted">
                              {formatParameters(action.parameters)}
                            </td>
                            <td className="small">
                              {formatDelay(action.delayMs)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </>
              )}
            </div>

            <div className="modal-footer py-2">
              {scene && onRunScene && (
                <button
                  type="button"
                  className="btn btn-primary btn-sm d-flex align-items-center gap-1"
                  onClick={() => onRunScene(scene.id)}
                  disabled={runningSceneId === scene.id}
                >
                  <FiPlay />
                  {runningSceneId === scene.id
                    ? "Se rulează..."
                    : "Rulează scenariul"}
                </button>
              )}

              <button
                type="button"
                className="btn btn-outline-secondary btn-sm"
                onClick={onClose}
              >
                Închide
              </button>
            </div>
          </div>
        </div>
      </div>

      <div className="modal-backdrop show" />
    </>
  );
}
