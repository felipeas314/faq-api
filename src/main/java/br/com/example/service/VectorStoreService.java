package br.com.example.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class VectorStoreService {

    private final WebClient webClient;
    private final String collection;

    public VectorStoreService(
            @Value("${qdrant.url}") String qdrantUrl,
            @Value("${qdrant.collection}") String collection) {
        this.webClient = WebClient.builder()
                .baseUrl(qdrantUrl)
                .build();
        this.collection = collection;
    }

    public Mono<Void> createCollectionIfNotExists() {
        Map<String, Object> config = Map.of(
                "vectors", Map.of(
                        "size", 1024,
                        "distance", "Cosine"
                )
        );

        return webClient.put()
                .uri("/collections/{collection}", collection)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(config)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    log.debug("Collection já existe ou erro ao criar: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<Void> upsert(String id, List<Double> vector, Map<String, Object> payload) {
        String pointId = uuidFromString(id);

        Map<String, Object> point = Map.of(
                "id", pointId,
                "vector", vector,
                "payload", payload
        );

        Map<String, Object> request = Map.of("points", List.of(point));

        return webClient.put()
                .uri("/collections/{collection}/points", collection)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Erro ao salvar vetor: {}", e.getMessage()));
    }

    public Flux<SearchResult> search(List<Double> queryVector, int limit) {
        Map<String, Object> request = Map.of(
                "vector", queryVector,
                "limit", limit,
                "with_payload", true
        );

        return webClient.post()
                .uri("/collections/{collection}/points/search", collection)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SearchResponse.class)
                .flatMapMany(response -> Flux.fromIterable(response.getResult()))
                .doOnError(e -> log.error("Erro na busca vetorial: {}", e.getMessage()));
    }

    public Mono<Void> delete(String id) {
        String pointId = uuidFromString(id);

        Map<String, Object> request = Map.of(
                "points", List.of(pointId)
        );

        return webClient.post()
                .uri("/collections/{collection}/points/delete", collection)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Erro ao deletar vetor: {}", e.getMessage()));
    }

    private String uuidFromString(String input) {
        return UUID.nameUUIDFromBytes(input.getBytes()).toString();
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResult {
        private String id;
        private Double score;
        private Map<String, Object> payload;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SearchResponse {
        @JsonProperty("result")
        private List<SearchResult> result;
    }
}
