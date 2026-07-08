import { useEffect, useMemo, useState } from "react";
import api from "../../api.ts";
import { useAuth } from "../auth/AuthContext.tsx";

type ParameterInputType = "SELECT" | "SLIDER" | "NUMBER";

interface DeviceResponse {
  id: number;
  name: string;
  room: string;
  type: string;
  integrationType: string;
  identifier: string;
  enabled: boolean;
}

interface OptionDto {
  label: string;
  value: string;
}

interface CommandParameterResponse {
  name: string;
  label: string;
  inputType: ParameterInputType;
  minValue: number | null;
  maxValue: number | null;
  unit: string | null;
  options: OptionDto[];
}

interface DeviceCommandOptionResponse {
  capability: string;
  command: string;
  label: string;
  parameters: CommandParameterResponse[];
}

interface DeviceOptionsResponse {
  deviceId: number;
  deviceName: string;
  deviceType: string;
  integrationType: string;
  actions: DeviceCommandOptionResponse[];
}

interface SceneActionRequest {
  deviceId: number;
  capability: string;
  commandType: string;
  parameters: Record<string, string | number | boolean>;
  executionOrder: number;
  delayMs: number;
}

interface SceneActionDraft extends SceneActionRequest {
  localId: string;
  deviceName: string;
  actionLabel: string;
}

interface CreateSceneRequest {
  name: string;
  description: string;
  actions: SceneActionRequest[];
}

interface SceneResponse {
  id: number;
  name: string;
  description: string;
  enabled: boolean;
  createdAt: string;
}

interface SceneBuilderProps {
  onSceneSaved?: (scene: SceneResponse) => void;
}

export default function SceneBuilder({ onSceneSaved }: SceneBuilderProps) {
  const [devices, setDevices] = useState<DeviceResponse[]>([]);
  const [selectedDeviceId, setSelectedDeviceId] = useState<number | null>(null);
  const [deviceOptions, setDeviceOptions] =
    useState<DeviceOptionsResponse | null>(null);

  const [selectedCapability, setSelectedCapability] = useState("");
  const [selectedCommand, setSelectedCommand] = useState("");
  const [parameterValues, setParameterValues] = useState<
    Record<string, string>
  >({});
  const [delaySeconds, setDelaySeconds] = useState("0");

  const [sceneName, setSceneName] = useState("");
  const [sceneDescription, setSceneDescription] = useState("");
  const [actions, setActions] = useState<SceneActionDraft[]>([]);

  const [createdSceneId, setCreatedSceneId] = useState<number | null>(null);
  const [actionModalOpen, setActionModalOpen] = useState(false);
  const [editingActionId, setEditingActionId] = useState<string | null>(null);

  const [loadingDevices, setLoadingDevices] = useState(false);
  const [loadingOptions, setLoadingOptions] = useState(false);
  const [saving, setSaving] = useState(false);
  const [running, setRunning] = useState(false);

  const isEditing = editingActionId !== null;
  const { isAdmin } = useAuth();

  useEffect(() => {
    fetchDevices();
  }, []);

  const selectedDevice = useMemo(() => {
    return devices.find((device) => device.id === selectedDeviceId) ?? null;
  }, [devices, selectedDeviceId]);

  const groupedActions = useMemo(() => {
    const groups: Record<string, DeviceCommandOptionResponse[]> = {};

    if (!deviceOptions) {
      return groups;
    }

    for (const action of deviceOptions.actions) {
      if (!groups[action.capability]) {
        groups[action.capability] = [];
      }
      groups[action.capability].push(action);
    }

    return groups;
  }, [deviceOptions]);

  const availableCapabilities = useMemo(() => {
    return Object.keys(groupedActions);
  }, [groupedActions]);

  const availableCommands = useMemo(() => {
    if (!selectedCapability) {
      return [];
    }

    return groupedActions[selectedCapability] ?? [];
  }, [groupedActions, selectedCapability]);

  const selectedAction = useMemo(() => {
    return (
      availableCommands.find((action) => action.command === selectedCommand) ??
      null
    );
  }, [availableCommands, selectedCommand]);

  async function fetchDevices() {
    try {
      setLoadingDevices(true);
      const response = await api.get<DeviceResponse[]>("api/devices");
      setDevices(response.data);
    } catch (error) {
      console.error("Eroare la încărcarea dispozitivelor:", error);
      alert("Nu s-au putut încărca dispozitivele.");
    } finally {
      setLoadingDevices(false);
    }
  }

  async function handleSelectDevice(deviceId: number, resetSelection = true) {
    setSelectedDeviceId(deviceId);
    setDeviceOptions(null);

    if (resetSelection) {
      setSelectedCapability("");
      setSelectedCommand("");
      setParameterValues({});
      setDelaySeconds("0");
    }

    try {
      setLoadingOptions(true);

      const response = await api.get<DeviceOptionsResponse>(
        `/api/device-options/${deviceId}/commands`,
      );

      setDeviceOptions(response.data);
      return response.data;
    } catch (error) {
      console.error("Eroare la încărcarea opțiunilor dispozitivului:", error);
      alert("Nu s-au putut încărca opțiunile pentru dispozitiv.");
      return null;
    } finally {
      setLoadingOptions(false);
    }
  }

  function handleCapabilityChange(capability: string) {
    setSelectedCapability(capability);

    const firstCommand = groupedActions[capability]?.[0];

    if (firstCommand) {
      setSelectedCommand(firstCommand.command);
      initializeParameters(firstCommand);
    } else {
      setSelectedCommand("");
      setParameterValues({});
    }
  }

  function handleCommandChange(command: string) {
    setSelectedCommand(command);

    const action = availableCommands.find((item) => item.command === command);

    if (action) {
      initializeParameters(action);
    } else {
      setParameterValues({});
    }
  }

  function initializeParameters(action: DeviceCommandOptionResponse) {
    const initialValues: Record<string, string> = {};

    for (const parameter of action.parameters) {
      if (parameter.inputType === "SELECT") {
        initialValues[parameter.name] = parameter.options?.[0]?.value ?? "";
      }

      if (
        parameter.inputType === "SLIDER" ||
        parameter.inputType === "NUMBER"
      ) {
        initialValues[parameter.name] = String(parameter.minValue ?? 0);
      }
    }

    setParameterValues(initialValues);
  }

  function handleParameterChange(name: string, value: string) {
    setParameterValues((prev) => ({
      ...prev,
      [name]: value,
    }));
  }

  function buildParameters(action: DeviceCommandOptionResponse) {
    const result: Record<string, string | number | boolean> = {};

    for (const parameter of action.parameters) {
      const rawValue = parameterValues[parameter.name];

      if (rawValue === undefined || rawValue === "") {
        throw new Error(`Parametrul ${parameter.label} este obligatoriu.`);
      }

      if (
        parameter.inputType === "SLIDER" ||
        parameter.inputType === "NUMBER"
      ) {
        const numericValue = Number(rawValue);

        if (Number.isNaN(numericValue)) {
          throw new Error(
            `Parametrul ${parameter.label} trebuie să fie numeric.`,
          );
        }

        if (parameter.minValue !== null && numericValue < parameter.minValue) {
          throw new Error(
            `${parameter.label} trebuie să fie cel puțin ${parameter.minValue}.`,
          );
        }

        if (parameter.maxValue !== null && numericValue > parameter.maxValue) {
          throw new Error(
            `${parameter.label} trebuie să fie cel mult ${parameter.maxValue}.`,
          );
        }

        result[parameter.name] = numericValue;
      } else {
        result[parameter.name] = rawValue;
      }
    }

    return result;
  }

  function parseDelaySecondsToMs() {
    if (delaySeconds.trim() === "") {
      return 0;
    }

    const seconds = Number(delaySeconds);

    if (Number.isNaN(seconds) || seconds < 0) {
      throw new Error("Delay-ul trebuie să fie un număr pozitiv sau 0.");
    }

    return Math.round(seconds * 1000);
  }

  function formatDelayMs(delayMs: number) {
    const seconds = delayMs / 1000;

    if (Number.isInteger(seconds)) {
      return `${seconds}s`;
    }

    return `${seconds.toFixed(2)}s`;
  }

  function msToSecondsInput(delayMs: number) {
    const seconds = delayMs / 1000;

    if (Number.isInteger(seconds)) {
      return String(seconds);
    }

    return String(seconds);
  }

  function createLocalId() {
    if (typeof crypto !== "undefined" && crypto.randomUUID) {
      return crypto.randomUUID();
    }

    return `${Date.now()}-${Math.random()}`;
  }

  function openAddModal() {
    setEditingActionId(null);
    setActionModalOpen(true);
  }

  async function openEditModal(action: SceneActionDraft) {
    setActionModalOpen(true);
    setEditingActionId(action.localId);

    await handleSelectDevice(action.deviceId, false);

    const stringParams: Record<string, string> = {};

    for (const [key, value] of Object.entries(action.parameters)) {
      stringParams[key] = String(value);
    }

    setSelectedCapability(action.capability);
    setSelectedCommand(action.commandType);
    setParameterValues(stringParams);
    setDelaySeconds(msToSecondsInput(action.delayMs));
  }

  function handleAddOrUpdateAction() {
    if (!selectedDevice || !selectedAction) {
      alert("Alege un dispozitiv și o comandă.");
      return;
    }

    try {
      const parameters = buildParameters(selectedAction);
      const delayMs = parseDelaySecondsToMs();

      if (isEditing && editingActionId) {
        setActions((prev) =>
          prev.map((action) =>
            action.localId === editingActionId
              ? {
                  ...action,
                  deviceId: selectedDevice.id,
                  deviceName: selectedDevice.name,
                  capability: selectedAction.capability,
                  commandType: selectedAction.command,
                  actionLabel: selectedAction.label,
                  parameters,
                  delayMs,
                }
              : action,
          ),
        );

        setEditingActionId(null);
        setActionModalOpen(false);
      } else {
        const newAction: SceneActionDraft = {
          localId: createLocalId(),
          deviceId: selectedDevice.id,
          deviceName: selectedDevice.name,
          capability: selectedAction.capability,
          commandType: selectedAction.command,
          actionLabel: selectedAction.label,
          parameters,
          executionOrder: actions.length + 1,
          delayMs,
        };

        setActions((prev) => [...prev, newAction]);
        setDelaySeconds("0");
      }

      setCreatedSceneId(null);
    } catch (error) {
      if (error instanceof Error) {
        alert(error.message);
      } else {
        alert("Acțiunea nu a putut fi salvată.");
      }
    }
  }

  function handleRemoveAction(localId: string) {
    setActions((prev) =>
      prev
        .filter((action) => action.localId !== localId)
        .map((action, index) => ({
          ...action,
          executionOrder: index + 1,
        })),
    );

    setCreatedSceneId(null);
  }

  async function handleSaveScene() {
    if (!sceneName.trim()) {
      alert("Introdu un nume pentru scenariu.");
      return;
    }

    if (actions.length === 0) {
      alert("Adaugă cel puțin o acțiune în scenariu.");
      return;
    }

    const payload: CreateSceneRequest = {
      name: sceneName.trim(),
      description: sceneDescription.trim(),
      actions: actions.map((action, index) => ({
        deviceId: action.deviceId,
        capability: action.capability,
        commandType: action.commandType,
        parameters: action.parameters,
        executionOrder: index + 1,
        delayMs: action.delayMs,
      })),
    };

    try {
      setSaving(true);

      const response = await api.post<SceneResponse>("/api/scenes", payload);

      setCreatedSceneId(response.data.id);
      onSceneSaved?.(response.data);

      alert(`Scenariul "${response.data.name}" a fost salvat.`);
    } catch (error) {
      console.error("Eroare la salvarea scenariului:", error);
      alert("Scenariul nu a putut fi salvat.");
    } finally {
      setSaving(false);
    }
  }

  async function handleRunCreatedScene() {
    if (!createdSceneId) {
      return;
    }

    try {
      setRunning(true);
      await api.post(`/api/scenes/${createdSceneId}/run`);
      alert("Scenariul a fost rulat.");
    } catch (error) {
      console.error("Eroare la rularea scenariului:", error);
      alert("Scenariul nu a putut fi rulat.");
    } finally {
      setRunning(false);
    }
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
    <div className="container py-3">
      <div className="d-flex align-items-center justify-content-between mb-3">
        <div>
          <h3 className="mb-1">Creare scenariu</h3>
          <p className="text-muted mb-0 small">
            Configurează o listă de acțiuni care vor fi executate în ordine.
          </p>
        </div>

        <button
          type="button"
          className="btn btn-primary btn-sm"
          onClick={openAddModal}
        >
          + Adaugă acțiune
        </button>
      </div>

      <div className="card border-0 shadow-sm mb-3">
        <div className="card-body py-3">
          <div className="row g-3">
            <div className="col-md-4">
              <label className="form-label small fw-semibold">
                Nume scenariu
              </label>
              <input
                type="text"
                className="form-control form-control-sm"
                value={sceneName}
                onChange={(event) => {
                  setSceneName(event.target.value);
                  setCreatedSceneId(null);
                }}
                placeholder="Ex: TV Time"
              />
            </div>

            <div className="col-md-8">
              <label className="form-label small fw-semibold">Descriere</label>
              <input
                type="text"
                className="form-control form-control-sm"
                value={sceneDescription}
                onChange={(event) => {
                  setSceneDescription(event.target.value);
                  setCreatedSceneId(null);
                }}
                placeholder="Ex: Pornește dispozitivele pentru vizionare TV"
              />
            </div>
          </div>
        </div>
      </div>

      <div className="card border-0 shadow-sm">
        <div className="card-header bg-white d-flex justify-content-between align-items-center py-2">
          <div>
            <span className="fw-semibold">Acțiuni</span>
            <span className="text-muted small ms-2">
              {actions.length} acțiuni
            </span>
          </div>

          {isAdmin && (
            <button
              type="button"
              className="btn btn-outline-primary btn-sm"
              onClick={openAddModal}
            >
              Adaugă
            </button>
          )}
        </div>

        <div className="card-body p-0">
          {actions.length === 0 && (
            <div className="p-3 text-muted small">
              Nu ai adăugat încă nicio acțiune. Apasă pe „Adaugă acțiune”.
            </div>
          )}

          {actions.length > 0 && (
            <div className="table-responsive">
              <table className="table table-sm align-middle mb-0">
                <thead className="table-light">
                  <tr>
                    <th style={{ width: "50px" }}>#</th>
                    <th>Dispozitiv</th>
                    <th>Capabilitate</th>
                    <th>Comandă</th>
                    <th>Parametri</th>
                    <th>Delay</th>
                    <th style={{ width: "150px" }}></th>
                  </tr>
                </thead>

                <tbody>
                  {actions.map((action) => (
                    <tr key={action.localId}>
                      <td>{action.executionOrder}</td>
                      <td>
                        <div className="fw-semibold small">
                          {action.deviceName}
                        </div>
                      </td>
                      <td>
                        <span className="badge text-bg-light border">
                          {action.capability}
                        </span>
                      </td>
                      <td className="small">{action.commandType}</td>
                      <td className="small text-muted">
                        {formatParameters(action.parameters)}
                      </td>
                      <td className="small">{formatDelayMs(action.delayMs)}</td>
                      <td>
                        <div className="d-flex gap-1">
                          <button
                            type="button"
                            className="btn btn-outline-secondary btn-sm"
                            onClick={() => openEditModal(action)}
                          >
                            Edit
                          </button>

                          <button
                            type="button"
                            className="btn btn-outline-danger btn-sm"
                            onClick={() => handleRemoveAction(action.localId)}
                          >
                            Șterge
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="card-footer bg-white d-flex align-items-center justify-content-between py-2">
          <div className="small text-muted">
            {createdSceneId
              ? `Scenariu salvat cu ID ${createdSceneId}`
              : "Scenariul nu este salvat încă."}
          </div>

          <div className="d-flex gap-2">
            <button
              type="button"
              className="btn btn-success btn-sm"
              onClick={handleSaveScene}
              disabled={saving}
            >
              {saving ? "Se salvează..." : "Salvează scenariul"}
            </button>

            <button
              type="button"
              className="btn btn-outline-primary btn-sm"
              onClick={handleRunCreatedScene}
              disabled={!createdSceneId || running}
            >
              {running ? "Se rulează..." : "Rulează"}
            </button>
          </div>
        </div>
      </div>

      {actionModalOpen && (
        <>
          <div className="modal show d-block" tabIndex={-1}>
            <div className="modal-dialog modal-xl modal-dialog-centered">
              <div className="modal-content">
                <div className="modal-header py-2">
                  <div>
                    <h5 className="modal-title mb-0">
                      {isEditing ? "Editează acțiunea" : "Adaugă acțiune"}
                    </h5>
                    <small className="text-muted">
                      Alege dispozitivul, capabilitatea, comanda și parametrii.
                    </small>
                  </div>

                  <button
                    type="button"
                    className="btn-close"
                    onClick={() => {
                      setActionModalOpen(false);
                      setEditingActionId(null);
                      setSelectedDeviceId(null);
                    }}
                  />
                </div>

                <div className="modal-body p-0">
                  <div className="row g-0" style={{ minHeight: "520px" }}>
                    <div className="col-md-4 border-end bg-light">
                      <div className="p-3 border-bottom">
                        <div className="fw-semibold small mb-1">
                          Dispozitive
                        </div>
                        <div className="text-muted small">
                          Selectează dispozitivul pentru care vrei să adaugi o
                          comandă.
                        </div>
                      </div>

                      <div
                        className="p-2"
                        style={{ maxHeight: "470px", overflowY: "auto" }}
                      >
                        {loadingDevices && (
                          <div className="text-muted small p-2">
                            Se încarcă dispozitivele...
                          </div>
                        )}

                        {!loadingDevices && devices.length === 0 && (
                          <div className="text-muted small p-2">
                            Nu există dispozitive salvate.
                          </div>
                        )}

                        <div className="list-group list-group-flush">
                          {devices.map((device) => (
                            <button
                              key={device.id}
                              type="button"
                              className={
                                selectedDeviceId === device.id
                                  ? "list-group-item list-group-item-action active rounded mb-1"
                                  : "list-group-item list-group-item-action rounded mb-1"
                              }
                              onClick={() => handleSelectDevice(device.id)}
                            >
                              <div className="fw-semibold small">
                                {device.name}
                              </div>
                              <div className="small">
                                {device.room} · {device.type}
                              </div>
                              <div className="small opacity-75 mt-1">
                                {device.integrationType}
                              </div>
                            </button>
                          ))}
                        </div>
                      </div>
                    </div>

                    <div className="col-md-8">
                      <div className="p-3">
                        {!selectedDevice && (
                          <div className="alert alert-light border small mb-0">
                            Selectează un dispozitiv din lista din stânga.
                          </div>
                        )}

                        {selectedDevice && loadingOptions && (
                          <div className="alert alert-light border small mb-0">
                            Se încarcă opțiunile pentru {selectedDevice.name}...
                          </div>
                        )}

                        {selectedDevice && deviceOptions && (
                          <>
                            <div className="d-flex justify-content-between align-items-start mb-3">
                              <div>
                                <div className="fw-semibold">
                                  {deviceOptions.deviceName}
                                </div>
                                <div className="text-muted small">
                                  {deviceOptions.deviceType} ·{" "}
                                  {deviceOptions.integrationType}
                                </div>
                              </div>

                              <span className="badge text-bg-light border">
                                {deviceOptions.actions.length} comenzi
                              </span>
                            </div>

                            <div className="row g-3">
                              <div className="col-md-6">
                                <label className="form-label small fw-semibold">
                                  Capabilitate
                                </label>
                                <select
                                  className="form-select form-select-sm"
                                  value={selectedCapability}
                                  onChange={(event) =>
                                    handleCapabilityChange(event.target.value)
                                  }
                                >
                                  <option value="">Alege capabilitatea</option>
                                  {availableCapabilities.map((capability) => (
                                    <option key={capability} value={capability}>
                                      {capability}
                                    </option>
                                  ))}
                                </select>
                              </div>

                              <div className="col-md-6">
                                <label className="form-label small fw-semibold">
                                  Comandă
                                </label>
                                <select
                                  className="form-select form-select-sm"
                                  value={selectedCommand}
                                  onChange={(event) =>
                                    handleCommandChange(event.target.value)
                                  }
                                  disabled={!selectedCapability}
                                >
                                  <option value="">Alege comanda</option>
                                  {availableCommands.map((action) => (
                                    <option
                                      key={`${action.capability}-${action.command}`}
                                      value={action.command}
                                    >
                                      {action.label} ({action.command})
                                    </option>
                                  ))}
                                </select>
                              </div>
                            </div>

                            {selectedAction && (
                              <div className="mt-3 p-3 border rounded bg-light">
                                <div className="fw-semibold small mb-2">
                                  Configurare comandă
                                </div>

                                {selectedAction.parameters.length === 0 && (
                                  <div className="text-muted small">
                                    Această comandă nu necesită parametri.
                                  </div>
                                )}

                                {selectedAction.parameters.map((parameter) => (
                                  <div className="mb-3" key={parameter.name}>
                                    <label className="form-label small fw-semibold">
                                      {parameter.label}
                                      {parameter.unit
                                        ? ` (${parameter.unit})`
                                        : ""}
                                    </label>

                                    {parameter.inputType === "SELECT" && (
                                      <select
                                        className="form-select form-select-sm"
                                        value={
                                          parameterValues[parameter.name] ?? ""
                                        }
                                        onChange={(event) =>
                                          handleParameterChange(
                                            parameter.name,
                                            event.target.value,
                                          )
                                        }
                                      >
                                        {parameter.options.map((option) => (
                                          <option
                                            key={option.value}
                                            value={option.value}
                                          >
                                            {option.label}
                                          </option>
                                        ))}
                                      </select>
                                    )}

                                    {parameter.inputType === "SLIDER" && (
                                      <div className="row g-2 align-items-center">
                                        <div className="col">
                                          <input
                                            type="range"
                                            className="form-range"
                                            min={parameter.minValue ?? 0}
                                            max={parameter.maxValue ?? 100}
                                            value={
                                              parameterValues[parameter.name] ??
                                              parameter.minValue ??
                                              0
                                            }
                                            onChange={(event) =>
                                              handleParameterChange(
                                                parameter.name,
                                                event.target.value,
                                              )
                                            }
                                          />
                                        </div>

                                        <div className="col-3">
                                          <input
                                            type="number"
                                            className="form-control form-control-sm"
                                            min={
                                              parameter.minValue ?? undefined
                                            }
                                            max={
                                              parameter.maxValue ?? undefined
                                            }
                                            value={
                                              parameterValues[parameter.name] ??
                                              ""
                                            }
                                            onChange={(event) =>
                                              handleParameterChange(
                                                parameter.name,
                                                event.target.value,
                                              )
                                            }
                                          />
                                        </div>
                                      </div>
                                    )}

                                    {parameter.inputType === "NUMBER" && (
                                      <input
                                        type="number"
                                        className="form-control form-control-sm"
                                        min={parameter.minValue ?? undefined}
                                        max={parameter.maxValue ?? undefined}
                                        value={
                                          parameterValues[parameter.name] ?? ""
                                        }
                                        onChange={(event) =>
                                          handleParameterChange(
                                            parameter.name,
                                            event.target.value,
                                          )
                                        }
                                      />
                                    )}
                                  </div>
                                ))}

                                <div className="row g-3 align-items-end">
                                  <div className="col-md-4">
                                    <label className="form-label small fw-semibold">
                                      Delay după acțiune (secunde)
                                    </label>
                                    <input
                                      type="number"
                                      className="form-control form-control-sm"
                                      min={0}
                                      step={0.5}
                                      value={delaySeconds}
                                      onChange={(event) =>
                                        setDelaySeconds(event.target.value)
                                      }
                                      placeholder="0"
                                    />
                                  </div>

                                  <div className="col-md-8"></div>
                                </div>
                              </div>
                            )}
                          </>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="modal-footer py-2">
                  <div className="me-auto text-muted small">
                    Acțiuni adăugate: {actions.length}
                  </div>

                  <button
                    type="button"
                    className="btn btn-outline-secondary btn-sm"
                    onClick={() => {
                      setActionModalOpen(false);
                      setEditingActionId(null);
                    }}
                  >
                    Închide
                  </button>

                  <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    onClick={handleAddOrUpdateAction}
                    disabled={!selectedAction}
                  >
                    {isEditing ? "Salvează modificările" : "Adaugă acțiunea"}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div className="modal-backdrop show" />
        </>
      )}
    </div>
  );
}
