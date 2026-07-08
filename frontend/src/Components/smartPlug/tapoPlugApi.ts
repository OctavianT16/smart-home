import axios from "axios";
import type { TapoPlugEntities } from "./tapoPlugTypes";

const tapoApi = axios.create({
  baseURL: "/api/tapo-plug",
  withCredentials: true,
});

export async function getTapoPlugEntities(): Promise<TapoPlugEntities> {
  const response = await tapoApi.get<TapoPlugEntities>("/entities");
  return response.data;
}

export async function getTapoPlugLiveEntities(): Promise<TapoPlugEntities> {
  const response = await tapoApi.get<TapoPlugEntities>("/live/entities");
  return response.data;
}

export async function turnOnTapoPlug(): Promise<void> {
  await tapoApi.post("/on");
}

export async function turnOffTapoPlug(): Promise<void> {
  await tapoApi.post("/off");
}

export async function toggleTapoPlug(): Promise<void> {
  await tapoApi.post("/toggle");
}
