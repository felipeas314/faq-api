package br.com.example.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmbeddingService {

    private final WebClient webClient;
    private final String model;

    public EmbeddingService(
            @Value("${ollama.url}") String ollamaUrl,
            @Value("${ollama.model}") String model) {
        this.webClient = WebClient.builder()
                .baseUrl(ollamaUrl)
                .build();
        this.model = model;
    }

    public Mono<List<Double>> generateEmbedding(String text) {
        Map<String, String> request = Map.of(
                "model", model,
                "prompt", text
        );

        return webClient.post()
                .uri("/api/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .map(EmbeddingResponse::getEmbedding)
                .doOnError(e -> log.error("Erro ao gerar embedding: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EmbeddingResponse {
        private List<Double> embedding;
    }
}
