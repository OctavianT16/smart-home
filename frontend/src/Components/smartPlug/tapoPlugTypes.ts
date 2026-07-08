export interface HaEntityState {
  entity_id: string;
  state: string;
  attributes: {
    friendly_name?: string;
    unit_of_measurement?: string;
    device_class?: string;
    state_class?: string;
    [key: string]: unknown;
  };
  last_changed: string;
  last_updated: string;
}

export interface TapoPlugEntities {
  switchEntity: HaEntityState | null;
  powerEntity: HaEntityState | null;
  energyEntity: HaEntityState | null;
  voltageEntity: HaEntityState | null;
  currentEntity: HaEntityState | null;
  lastUpdated: string | null;
}

export type TapoSocketStatus =
  | "connecting"
  | "connected"
  | "disconnected"
  | "error";
