package com.smartHome.backend.climate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public class GreeClient {

    private final ObjectMapper om;
    private final InetAddress host;
    private final int port;
    private final Duration timeout;

    public GreeClient(ObjectMapper om, String host, int port, Duration timeout) throws Exception {
        this.om = om;
        this.host = InetAddress.getByName(host);
        this.port = port;
        this.timeout = timeout;
    }

    public JsonNode bind(String macNormalized, String genericKey) throws Exception {
        String inner = "{\"mac\":\"" + macNormalized + "\",\"t\":\"bind\",\"uid\":0}";
        String pack = encryptPack(inner, genericKey);

        String outer = "{"
                + "\"cid\":\"app\","
                + "\"i\":1,"
                + "\"pack\":\"" + pack + "\","
                + "\"t\":\"pack\","
                + "\"tcid\":\"" + macNormalized + "\","
                + "\"uid\":0"
                + "}";

        JsonNode respOuter = sendAndReceiveJson(outer);

        String respPack = respOuter.path("pack").asText(null);
        if (respPack == null) return null;

        String decrypted = decryptPack(respPack, genericKey);
        return om.readTree(decrypted);
    }

    public JsonNode cmd(String macNormalized, String deviceKey, String[] opt, int[] p) throws Exception {
        // {"opt":["Pow","Mod","SetTem","WdSpd"],"p":[1,2,24,3],"t":"cmd"}
        StringBuilder optArr = new StringBuilder("[");
        for (int i = 0; i < opt.length; i++) {
            if (i > 0) optArr.append(",");
            optArr.append("\"").append(opt[i]).append("\"");
        }
        optArr.append("]");

        StringBuilder pArr = new StringBuilder("[");
        for (int i = 0; i < p.length; i++) {
            if (i > 0) pArr.append(",");
            pArr.append(p[i]);
        }
        pArr.append("]");

        String inner = "{"
                + "\"opt\":" + optArr + ","
                + "\"p\":" + pArr + ","
                + "\"t\":\"cmd\""
                + "}";

        String pack = encryptPack(inner, deviceKey);

        String outer = "{"
                + "\"cid\":\"app\","
                + "\"i\":0,"
                + "\"pack\":\"" + pack + "\","
                + "\"t\":\"pack\","
                + "\"tcid\":\"" + macNormalized + "\","
                + "\"uid\":0"
                + "}";

        JsonNode respOuter = sendAndReceiveJson(outer);
        String respPack = respOuter.path("pack").asText(null);
        if (respPack == null) return null;

        String decrypted = decryptPack(respPack, deviceKey);
        return om.readTree(decrypted);
    }

    private JsonNode sendAndReceiveJson(String json) throws Exception {
        try (DatagramSocket sock = new DatagramSocket()) {
            sock.setSoTimeout((int) timeout.toMillis());

            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            DatagramPacket p = new DatagramPacket(data, data.length, host, port);
            sock.send(p);

            byte[] buf = new byte[8192];
            DatagramPacket resp = new DatagramPacket(buf, buf.length);
            sock.receive(resp);

            String respJson = new String(resp.getData(), 0, resp.getLength(), StandardCharsets.UTF_8);
            return om.readTree(respJson);
        }
    }

    private static String encryptPack(String plainJson, String key16) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key16.getBytes(StandardCharsets.UTF_8), "AES"));
        byte[] enc = cipher.doFinal(plainJson.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(enc);
    }

    private static String decryptPack(String packB64, String key16) throws Exception {
        byte[] enc = Base64.getDecoder().decode(packB64);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key16.getBytes(StandardCharsets.UTF_8), "AES"));
        byte[] dec = cipher.doFinal(enc);
        return new String(dec, StandardCharsets.UTF_8);
    }
}
