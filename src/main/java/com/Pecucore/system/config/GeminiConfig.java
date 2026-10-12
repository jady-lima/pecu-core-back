package com.Pecucore.system.config;

import com.Pecucore.system.ia.GeminiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiConfig {

    private static final Duration TIMEOUT_CONEXAO = Duration.ofSeconds(10);

    @Bean
    public RestClient geminiRestClient(GeminiProperties geminiProperties) {
        HttpClient httpClient = HttpClient.newBuilder()
                                          .connectTimeout(TIMEOUT_CONEXAO)
                                          .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(geminiProperties.timeout());

        return RestClient.builder()
                         .baseUrl(geminiProperties.url())
                         .requestFactory(requestFactory)
                         .build();
    }
}
