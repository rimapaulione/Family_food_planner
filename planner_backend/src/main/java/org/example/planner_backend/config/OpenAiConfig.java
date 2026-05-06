package org.example.planner_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAiConfig {

    private static final String OPENAI_BASE_URL = "https://api.openai.com/v1";

    @Bean
    public RestClient openAiRestClient(@Value("${openai.api-key}") final String apiKey) {
        return RestClient.builder()
                .baseUrl(OPENAI_BASE_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }
}
