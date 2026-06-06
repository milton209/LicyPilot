package com.licypilot.backend.config;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GitHubAiConfig {

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Bean
    public OpenAiApi openAiApi(RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder) {
        // Customiza o RestClient.Builder para incluir os headers do GitHub
        restClientBuilder
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("Accept", "application/vnd.github+json");

        // Customiza o WebClient.Builder (necessário para o construtor, embora o ChatModel use RestClient por padrão)
        webClientBuilder
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("Accept", "application/vnd.github+json");

        return new OpenAiApi(baseUrl, apiKey, restClientBuilder, webClientBuilder);
    }
}
