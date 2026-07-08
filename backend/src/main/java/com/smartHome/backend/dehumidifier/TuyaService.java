package com.smartHome.backend.dehumidifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
public class TuyaService {

    private static final String POWER_CODE = "switch";
    private static final String MODE_CODE = "mode";
    private static final String HUMIDITY_CODE = "dehumidify_set_value";
    private static final String FAN_SPEED_CODE = "fan_speed_enum";
    private static final String COUNTDOWN_CODE = "countdown_set";

    private final TuyaProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TuyaService(TuyaProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void turnOn() {
        sendPowerCommand(true);
    }

    public void turnOff() {
        sendPowerCommand(false);
    }

    public void setMode(TuyaMode mode) {
        sendDeviceCommand(List.of(
                new TuyaCommandRequest.Command(MODE_CODE, mode.getValue())
        ));
    }

    public void setMode(String mode) {
        setMode(TuyaMode.fromValue(mode));

    }

    private void sendPowerCommand(boolean state) {
        sendDeviceCommand(List.of(
                new TuyaCommandRequest.Command(POWER_CODE, state)
        ));
    }

    private void sendDeviceCommand(List<TuyaCommandRequest.Command> commands) {
        String token = getAccessToken();

        String path = "/v1.0/iot-03/devices/" + properties.getDeviceId() + "/commands";
        String url = properties.getBaseUrl() + path;

        TuyaCommandRequest request = new TuyaCommandRequest(commands);
        String bodyJson = toJson(request);

        HttpHeaders headers = buildSignedHeaders(
                HttpMethod.POST,
                path,
                bodyJson,
                token
        );

        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public void setTargetHumidity(Integer humidity) {
        validateHumidity(humidity);

        sendDeviceCommand(List.of(
                new TuyaCommandRequest.Command(HUMIDITY_CODE, humidity)
        ));
    }

    public void setFanSpeed(TuyaFanSpeed fanSpeed) {
        sendDeviceCommand(List.of(
                new TuyaCommandRequest.Command(FAN_SPEED_CODE, fanSpeed.getValue())
        ));
    }

    public void setFanSpeed(String fanSpeed) {
        setFanSpeed(TuyaFanSpeed.fromValue(fanSpeed));
    }

    public void setCountdown(TuyaCountdown countdown) {
        sendDeviceCommand(List.of(
                new TuyaCommandRequest.Command(COUNTDOWN_CODE, countdown.getValue())
        ));
    }

    public void setCountdown(String countdown) {
        setCountdown(TuyaCountdown.fromValue(countdown));
    }

    private void validateHumidity(Integer humidity) {
        if (humidity == null) {
            throw new IllegalArgumentException("Valoarea umidității nu poate fi nulă");
        }

        if (humidity < 25 || humidity > 80) {
            throw new IllegalArgumentException("Umiditatea trebuie să fie între 25% și 80%");
        }

        if (humidity % 5 != 0) {
            throw new IllegalArgumentException("Umiditatea trebuie să fie multiplu de 5");
        }
    }


    private String getAccessToken() {
        String path = "/v1.0/token?grant_type=1";
        String url = properties.getBaseUrl() + path;

        HttpHeaders headers = buildSignedHeaders(
                HttpMethod.GET,
                path,
                "",
                null
        );

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<TuyaAuthResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                TuyaAuthResponse.class
        );

        if (response.getBody() == null || response.getBody().getResult() == null) {
            throw new RuntimeException("Nu s-a putut obține access token-ul de la Tuya");
        }

        return response.getBody().getResult().getAccess_token();
    }

    private HttpHeaders buildSignedHeaders(
            HttpMethod method,
            String path,
            String bodyJson,
            String accessToken
    ) {
        String safeBody = bodyJson == null ? "" : bodyJson;
        String contentSha256 = sha256(safeBody);

        String stringToSign = method.name() + "\n" +
                contentSha256 + "\n" +
                "" + "\n" +
                path;

        long timestamp = System.currentTimeMillis();

        String dataToSign;

        if (accessToken == null || accessToken.isBlank()) {
            dataToSign = properties.getClientId() + timestamp + stringToSign;
        } else {
            dataToSign = properties.getClientId() + accessToken + timestamp + stringToSign;
        }

        String sign = generateSign(dataToSign);

        HttpHeaders headers = new HttpHeaders();
        headers.add("client_id", properties.getClientId());
        headers.add("t", String.valueOf(timestamp));
        headers.add("sign", sign);
        headers.add("sign_method", "HMAC-SHA256");

        if (accessToken != null && !accessToken.isBlank()) {
            headers.add("access_token", accessToken);
        }

        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        return headers;
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Eroare la serializarea request-ului Tuya", e);
        }
    }

    private String generateSign(String data) {
        return new HmacUtils("HmacSHA256", properties.getSecret())
                .hmacHex(data)
                .toUpperCase();
    }

    private String sha256(String body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(body.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(hash).toLowerCase();
        } catch (Exception e) {
            throw new RuntimeException("Eroare la calcularea SHA-256", e);
        }
    }

    public DehumidifierStatusResponse getDehumidifierStatus() {
        String token = getAccessToken();

        String path = "/v1.0/iot-03/devices/" + properties.getDeviceId() + "/status";
        String url = properties.getBaseUrl() + path;

        HttpHeaders headers = buildSignedHeaders(
                HttpMethod.GET,
                path,
                "",
                token
        );

        HttpEntity<String> entity = new HttpEntity<>(null, headers);

        ResponseEntity<TuyaDeviceStatusResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                TuyaDeviceStatusResponse.class
        );

        TuyaDeviceStatusResponse body = response.getBody();

        if (body == null || body.getResult() == null) {
            throw new RuntimeException("Nu s-a putut obține statusul dezumidificatorului din Tuya");
        }

        return new DehumidifierStatusResponse(
                asBoolean(findStatusValue(body, POWER_CODE)),
                asString(findStatusValue(body, MODE_CODE)),
                asInteger(findStatusValue(body, HUMIDITY_CODE)),
                asString(findStatusValue(body, FAN_SPEED_CODE)),
                asString(findStatusValue(body, COUNTDOWN_CODE))
        );
    }

    private Object findStatusValue(TuyaDeviceStatusResponse response, String code) {
        return response.getResult()
                .stream()
                .filter(item -> code.equals(item.getCode()))
                .map(TuyaDeviceStatusResponse.StatusItem::getValue)
                .findFirst()
                .orElse(null);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Boolean asBoolean(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return Boolean.parseBoolean(String.valueOf(value));
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        return Integer.parseInt(String.valueOf(value));
    }
}