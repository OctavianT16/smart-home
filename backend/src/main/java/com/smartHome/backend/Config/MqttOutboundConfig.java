package com.smartHome.backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.UUID;

@Configuration
public class MqttOutboundConfig {

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutboundHandler(MqttPahoClientFactory mqttClientFactory) {

        String outboundClientId = "spring-" + UUID.randomUUID() + "-outbound";

        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(outboundClientId, mqttClientFactory);

        handler.setAsync(true);
        handler.setDefaultQos(1);

        return handler;
    }
}
