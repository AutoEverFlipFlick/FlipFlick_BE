package com.flipflick.backend.common.config.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient tmdbWebClient(WebClient.Builder builder,
                                   @Value("${tmdb.api.url}") String baseUrl) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        // 10 * 1024 * 1024 = 10MB
                        .maxInMemorySize(10 * 1024 * 1024)
                )
                .build();

        return builder
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean
    public WebClient kobisClient() {
        return WebClient.builder()
                .baseUrl("https://www.kobis.or.kr")
                .build();
    }
}
