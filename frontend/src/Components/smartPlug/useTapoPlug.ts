import { useCallback, useEffect, useRef, useState } from "react";
import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";

import {
  getTapoPlugEntities,
  turnOffTapoPlug,
  turnOnTapoPlug,
} from "./tapoPlugApi";

import type {
  HaEntityState,
  TapoPlugEntities,
  TapoSocketStatus,
} from "./tapoPlugTypes";

const emptyEntities: TapoPlugEntities = {
  switchEntity: null,
  powerEntity: null,
  energyEntity: null,
  voltageEntity: null,
  currentEntity: null,
  lastUpdated: null,
};

function mergeEntityUpdate(
  previous: TapoPlugEntities,
  updatedEntity: HaEntityState,
): TapoPlugEntities {
  const entityId = updatedEntity.entity_id;

  if (previous.switchEntity?.entity_id === entityId) {
    return {
      ...previous,
      switchEntity: updatedEntity,
      lastUpdated: updatedEntity.last_updated,
    };
  }

  if (previous.powerEntity?.entity_id === entityId) {
    return {
      ...previous,
      powerEntity: updatedEntity,
      lastUpdated: updatedEntity.last_updated,
    };
  }

  if (previous.energyEntity?.entity_id === entityId) {
    return {
      ...previous,
      energyEntity: updatedEntity,
      lastUpdated: updatedEntity.last_updated,
    };
  }

  if (previous.voltageEntity?.entity_id === entityId) {
    return {
      ...previous,
      voltageEntity: updatedEntity,
      lastUpdated: updatedEntity.last_updated,
    };
  }

  if (previous.currentEntity?.entity_id === entityId) {
    return {
      ...previous,
      currentEntity: updatedEntity,
      lastUpdated: updatedEntity.last_updated,
    };
  }

  return previous;
}

export function useTapoPlug() {
  const [entities, setEntities] = useState<TapoPlugEntities>(emptyEntities);
  const [loading, setLoading] = useState(true);
  const [commandPending, setCommandPending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [socketStatus, setSocketStatus] =
    useState<TapoSocketStatus>("disconnected");

  const stompClientRef = useRef<Client | null>(null);

  const refreshEntities = useCallback(async () => {
    try {
      setError(null);
      const snapshot = await getTapoPlugEntities();
      setEntities(snapshot);
    } catch (err) {
      console.error(err);
      setError("Nu s-au putut încărca datele prizei.");
    } finally {
      setLoading(false);
    }
  }, []);

  const turnOn = useCallback(async () => {
    try {
      setCommandPending(true);
      setError(null);
      await turnOnTapoPlug();
    } catch (err) {
      console.error(err);
      setError("Nu s-a putut porni priza.");
    } finally {
      setCommandPending(false);
    }
  }, []);

  const turnOff = useCallback(async () => {
    try {
      setCommandPending(true);
      setError(null);
      await turnOffTapoPlug();
    } catch (err) {
      console.error(err);
      setError("Nu s-a putut opri priza.");
    } finally {
      setCommandPending(false);
    }
  }, []);

  const toggle = useCallback(async () => {
    const isOn = entities.switchEntity?.state === "on";

    if (isOn) {
      await turnOff();
    } else {
      await turnOn();
    }
  }, [entities.switchEntity?.state, turnOff, turnOn]);

  useEffect(() => {
    refreshEntities();
  }, [refreshEntities]);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        setSocketStatus("connected");

        client.subscribe("/topic/tapo-plug/entity", (message: IMessage) => {
          try {
            const updatedEntity = JSON.parse(message.body) as HaEntityState;

            setEntities((previous) =>
              mergeEntityUpdate(previous, updatedEntity),
            );
          } catch (err) {
            console.error("Invalid Tapo WebSocket message", err);
          }
        });
      },

      onWebSocketClose: () => {
        setSocketStatus("disconnected");
      },

      onStompError: (frame) => {
        console.error("STOMP error", frame);
        setSocketStatus("error");
        setError("Eroare la conexiunea WebSocket.");
      },

      onWebSocketError: (event) => {
        console.error("WebSocket error", event);
        setSocketStatus("error");
      },
    });

    setSocketStatus("connecting");
    client.activate();
    stompClientRef.current = client;

    return () => {
      client.deactivate();
      stompClientRef.current = null;
    };
  }, []);

  return {
    entities,
    loading,
    error,
    socketStatus,
    commandPending,
    refreshEntities,
    turnOn,
    turnOff,
    toggle,
  };
}
