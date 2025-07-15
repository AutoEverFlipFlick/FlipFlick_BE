package com.flipflick.backend.common.config.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${tmdb.api.url}")
    private String apiUrl;

    @Bean
    public WebClient tmdbWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(apiUrl)
                .build();
    }
}