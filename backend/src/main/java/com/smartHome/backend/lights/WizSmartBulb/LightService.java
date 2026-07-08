package com.smartHome.backend.lights.WizSmartBulb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@Service
public class LightService {

    private static final int PORT = 38899;
    private final ObjectMapper objectMapper;

    public LightService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void sendLightCommand(String ip, boolean turnOn, Integer brightness, Integer temp) {
        StringBuilder command = new StringBuilder();
        command.append("{\"method\":\"setPilot\",\"params\":{");
        command.append("\"state\":").append(turnOn);
        if (temp != null) {
            command.append(",\"temp\":").append(temp);
        }
        if (brightness != null) {
            command.append(",\"dimming\":").append(brightness);
        }
        command.append("}}");
        sendUdp(ip, command.toString());
    }

    private static final int FIXED_SPEED = 125;


    public void applyMode(String ip, LightMode mode) {
        String payload = buildScenePayload(mode.sceneId, mode.needsSpeed ? FIXED_SPEED : null);
        sendUdp(ip, payload);
    }


    private String buildScenePayload(int sceneId, Integer speedOrNull) {

        StringBuilder sb = new StringBuilder();
        sb.append("{\"method\":\"setPilot\",\"params\":{");
        sb.append("\"sceneId\":").append(sceneId);

        if (speedOrNull != null) {
            sb.append(",\"speed\":").append(speedOrNull);

        }

        sb.append("}}");
        return sb.toString();
    }

    private void sendUdp(String ip, String payload) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(ip);
            byte[] data = payload.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(data, data.length, address, PORT);
            socket.send(packet);
            System.out.println("Sent to " + ip + " -> " + payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send UDP to " + ip, e);
        }
    }

    public WizPilotStateResponse getPilot(String ip) {
        String command = "{\"method\":\"getPilot\",\"params\":{}}";

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000);

            InetAddress address = InetAddress.getByName(ip);
            byte[] data = command.getBytes(StandardCharsets.UTF_8);

            DatagramPacket packet = new DatagramPacket(data, data.length, address, PORT);
            socket.send(packet);

            byte[] buffer = new byte[2048];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);

            String response = new String(
                    responsePacket.getData(),
                    0,
                    responsePacket.getLength(),
                    StandardCharsets.UTF_8
            );

            System.out.println("WiZ getPilot response: " + response);

            JsonNode root = objectMapper.readTree(response);

            JsonNode pilotData = root.has("result")
                    ? root.path("result")
                    : root.path("params");

            boolean isOn = pilotData.path("state").asBoolean(false);

            Integer temp = pilotData.path("temp").asInt();
            Integer brightness = pilotData.path("dimming").asInt();

            Integer sceneId = pilotData.has("sceneId") && !pilotData.get("sceneId").isNull()
                    ? pilotData.get("sceneId").asInt()
                    : null;

            String mode = resolveMode(sceneId);

            return new WizPilotStateResponse(isOn,temp,brightness ,mode.toLowerCase());


        } catch (Exception e) {
            throw new RuntimeException("Nu s-a putut citi starea becului WiZ", e);
        }
    }

    private String resolveMode(Integer sceneId) {
        if (sceneId == null) {
            return "CUSTOM";
        }

        return LightMode.fromSceneId(sceneId)
                .map(Enum::name)
                .orElse("CUSTOM");
    }
}



