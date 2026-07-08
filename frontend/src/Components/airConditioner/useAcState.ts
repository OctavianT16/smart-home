import { useEffect, useRef, useState } from "react";
import { Client, type IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import type { AcState } from "./acApi";

type Conn = "disconnected" | "connecting" | "connected" | "error";

export const useAcState = () => {
  const [state, setState] = useState<AcState | null>(null);
  const [conn, setConn] = useState<Conn>("disconnected");
  const [error, setError] = useState<string | null>(null);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    setConn("connecting");
    setError(null);

    const client = new Client({
      webSocketFactory: () => new SockJS("/ws"),
      reconnectDelay: 2000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        setConn("connected");
        client.subscribe("/topic/ac/state", (msg: IMessage) => {
          try {
            setState(JSON.parse(msg.body) as AcState);
          } catch {}
        });
      },

      onStompError: (frame) => {
        setConn("error");
        setError(frame.headers["message"] || "STOMP error");
      },
      onWebSocketError: () => {
        setConn("error");
        setError("WebSocket error");
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, []);

  return { state, conn, error, setState };
};
