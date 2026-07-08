package com.smartHome.backend.lights.esp32Flower;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class FlowerCommandService {

    private final MessageChannel mqttOutboundChannel;

    public FlowerCommandService(@Qualifier("mqttOutboundChannel") MessageChannel mqttOutboundChannel) {
        this.mqttOutboundChannel = mqttOutboundChannel;
    }

    public void setPower(String deviceId, boolean on) {
        String topic = "smarthome/actuators/" + deviceId + "/flower/power";
        publish(topic, on ? "on" : "off", 1, false);
    }

    public void setBrightness(String deviceId, int value0to255) {
        int v = Math.max(0, Math.min(255, value0to255));
        String topic = "smarthome/actuators/" + deviceId + "/flower/brightness";
        publish(topic, String.valueOf(v), 1, false);
    }

    public int pctTo255(int pct) {
        int p = Math.max(10, Math.min(100, pct));
        double norm = p / 100.0;
        return (int) Math.round(norm * 255);
    }

    private void publish(String topic, String payload, int qos, boolean retained) {
        Message<String> msg = MessageBuilder.withPayload(payload)
                .setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, qos)
                .setHeader(MqttHeaders.RETAINED, retained)
                .build();

        mqttOutboundChannel.send(msg);
    }
}
