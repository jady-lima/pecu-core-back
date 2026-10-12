package com.Pecucore.system.ia;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ia.gemini")
public record GeminiProperties(
        String apiKey,
        String modelo,
        String url,
        Duration timeout
) {
    public boolean possuiApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
