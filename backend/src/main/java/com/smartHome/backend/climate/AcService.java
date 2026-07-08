package com.smartHome.backend.climate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AcService {

    private final GreeProperties props;
    private final ObjectMapper om;
    private final SimpMessagingTemplate messagingTemplate;


    private final GreeClient client;


    private volatile String deviceKey;
    private volatile Instant keyUpdatedAt;

    private final AtomicReference<AcState> state = new AtomicReference<>(AcState.defaults());

    private static final Duration KEY_TTL = Duration.ofHours(72);

    public AcService(GreeProperties props, ObjectMapper om,SimpMessagingTemplate messagingTemplate) throws Exception {
        this.props = props;
        this.om = om;
        this.client = new GreeClient(om, props.host(), props.port() != null ? props.port() : 7000, Duration.ofSeconds(3));
        this.messagingTemplate = messagingTemplate;
    }

    public void setPower(boolean on, String updatedBy) throws Exception {
        ensureKey();
        cmd(new String[]{"Pow"}, new int[]{on ? 1 : 0});
        updateState(s-> s.withPower(on, updatedBy));
    }

    public void setMode(AcDtos.Mode mode, String update) throws Exception {
        ensureKey();
        int mod = mapMode(mode);
        cmd(new String[]{"Mod"}, new int[]{mod});
        updateState(s -> s.withMode(mode, update));
    }

    public void setTemperature(int celsius,String update) throws Exception {
        ensureKey();
        cmd(new String[]{"SetTem"}, new int[]{celsius});
        updateState(s -> s.withTemperatureC(celsius, update));

    }

    public void setAutoEnabled(boolean enabled, String updatedBy) {
        updateState(s -> s.withAutoEnabled(enabled, updatedBy));
    }

    public void setTargetAmbientTemperature(int targetC, String updatedBy) {
        updateState(s -> s.withTargetAmbientTemperatureC(targetC, updatedBy));
    }


    public void setFan(int level,String update) throws Exception {
        ensureKey();
        cmd(new String[]{"WdSpd"}, new int[]{level});
        updateState(s ->s.withFanLevel(level, update));

    }

    public void setCoolingMode(boolean on, AcDtos.Mode mode,int fanLevel , int temperature, String updatedBy) throws Exception {
        ensureKey();
        int mod = mapMode(mode);
        cmd(new String[]{"Pow","Mod","WdSpd","SetTem"}, new int[]{on? 1:0,mod,fanLevel,temperature});
        updateState(s -> s.withPower(on, updatedBy)
                .withMode(mode, updatedBy)
                .withFanLevel(fanLevel, updatedBy)
                .withTemperatureC(temperature, updatedBy));
    }

    public void applySceneState(
            boolean power,
            AcDtos.Mode mode,
            int temperatureC,
            int fanLevel,
            boolean autoEnabled
    ) throws Exception {
        ensureKey();

        int mod = mapMode(mode);

        cmd(
                new String[]{"Pow", "Mod", "WdSpd", "SetTem"},
                new int[]{power ? 1 : 0, mod, fanLevel, temperatureC}
        );

        updateState(s -> s.withPower(power, "scene")
                .withMode(mode, "scene")
                .withFanLevel(fanLevel, "scene")
                .withTemperatureC(temperatureC, "scene")
                .withAutoEnabled(autoEnabled, "scene"));
    }

    private synchronized void ensureKey() throws Exception {

        if (this.deviceKey == null && props.deviceKey() != null && props.deviceKey().length() == 16) {
            this.deviceKey = props.deviceKey();
            this.keyUpdatedAt = Instant.now();
            return;
        }

        if (deviceKey != null && keyUpdatedAt != null && keyUpdatedAt.plus(KEY_TTL).isAfter(Instant.now())) {
            return;
        }

        String mac = normalizeMac(props.mac());
        JsonNode bindResp = client.bind(mac, props.genericKey());
        if (bindResp == null) {
            throw new IllegalStateException("Bind failed (no response). Check IP/MAC, UDP, Wi-Fi isolation.");
        }

        if (!"bindok".equals(bindResp.path("t").asText())) {
            throw new IllegalStateException("Bind unexpected response: " + bindResp.toString());
        }
        if (bindResp.path("r").asInt(-1) != 200) {
            throw new IllegalStateException("Bind failed r=" + bindResp.path("r").asInt(-1) + " resp=" + bindResp);
        }

        String key = bindResp.path("key").asText(null);
        if (key == null || key.length() != 16) {
            throw new IllegalStateException("Invalid device key from bind: " + key);
        }

        this.deviceKey = key;
        this.keyUpdatedAt = Instant.now();
        System.out.println(deviceKey);
    }

    private void cmd(String[] opt, int[] p) throws Exception {
        String mac = normalizeMac(props.mac());
        JsonNode resp = client.cmd(mac, deviceKey, opt, p);

        // Expect: {"t":"res","r":200,...}
        if (resp == null) throw new IllegalStateException("No response from AC");
        if (!"res".equals(resp.path("t").asText())) {
            throw new IllegalStateException("Unexpected cmd response: " + resp);
        }
        if (resp.path("r").asInt(-1) != 200) {
            throw new IllegalStateException("Command rejected r=" + resp.path("r").asInt(-1) + " resp=" + resp);
        }
    }

    private static int mapMode(AcDtos.Mode mode) {
        return switch (mode) {
            case AUTO -> 0;
            case COOL -> 1;
            case DRY  -> 2;
            case FAN  -> 3;
            case HEAT -> 4;
        };
    }

    private static String normalizeMac(String mac) {
        if (mac == null) throw new IllegalArgumentException("gree.mac missing");
        String cleaned = mac.replace(":", "").replace("-", "").trim();
        return cleaned.toLowerCase(Locale.ROOT);
    }

    public AcState getState() {
        return state.get();
    }

    private void updateState(java.util.function.UnaryOperator<AcState> fn) {

        AcState st = state.updateAndGet(fn);
        messagingTemplate.convertAndSend("/topic/ac/state", st);
    }

    private void publishState(AcState st) {
        messagingTemplate.convertAndSend("/topic/ac/state", st);
    }


}


