package com.smartHome.backend.Config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(TapoHomeAssistantProperties.class)
public class TapoHomeAssistantConfig {

    @Bean
    public WebClient tapoHomeAssistantWebClient(TapoHomeAssistantProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.token())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}