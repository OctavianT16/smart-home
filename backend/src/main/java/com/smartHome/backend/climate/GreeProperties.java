package com.smartHome.backend.climate;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gree")
public record GreeProperties(
        String host,
        Integer port,
        String mac,
        String genericKey,
        String deviceKey
) {}
