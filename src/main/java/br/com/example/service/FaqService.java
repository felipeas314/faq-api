package br.com.example.service;

import br.com.example.dto.request.FaqRequest;
import br.com.example.dto.request.SearchRequest;
import br.com.example.dto.response.FaqResponse;
import br.com.example.dto.response.SearchResponse;
import br.com.example.exception.NotFoundException;
import br.com.example.model.Faq;
import br.com.example.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public Mono<FaqResponse> create(FaqRequest request, String username) {
        Faq faq = Faq.builder()
                .pergunta(request.getPergunta())
                .resposta(request.getResposta())
                .categoria(request.getCategoria())
                .tags(request.getTags() != null ? request.getTags() : List.of())
                .criadoPor(username)
                .build();

        return faqRepository.save(faq)
                .flatMap(savedFaq -> indexFaq(savedFaq).thenReturn(savedFaq))
                .map(FaqResponse::fromEntity);
    }

    public Flux<FaqResponse> findAll() {
        return faqRepository.findAll()
                .map(FaqResponse::fromEntity);
    }

    public Mono<FaqResponse> findById(String id) {
        return faqRepository.findById(id)
                .map(FaqResponse::fromEntity)
                .switchIfEmpty(Mono.error(new NotFoundException("FAQ não encontrada")));
    }

    public Mono<FaqResponse> update(String id, FaqRequest request, String username) {
        return faqRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("FAQ não encontrada")))
                .flatMap(faq -> {
                    faq.setPergunta(request.getPergunta());
                    faq.setResposta(request.getResposta());
                    faq.setCategoria(request.getCategoria());
                    faq.setTags(request.getTags() != null ? request.getTags() : List.of());
                    return faqRepository.save(faq);
                })
                .flatMap(savedFaq -> indexFaq(savedFaq).thenReturn(savedFaq))
                .map(FaqResponse::fromEntity);
    }

    public Mono<Void> delete(String id) {
        return faqRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("FAQ não encontrada")))
                .flatMap(faq -> vectorStoreService.delete(faq.getId())
                        .then(faqRepository.delete(faq)));
    }

    public Flux<SearchResponse> search(SearchRequest request) {
        return embeddingService.generateEmbedding(request.getQuery())
                .flatMapMany(vector -> vectorStoreService.search(vector, request.getLimit()))
                .flatMap(result -> {
                    String faqId = (String) result.getPayload().get("faqId");
                    return faqRepository.findById(faqId)
                            .map(faq -> SearchResponse.builder()
                                    .faq(FaqResponse.fromEntity(faq))
                                    .score(result.getScore())
                                    .build());
                });
    }

    private Mono<Void> indexFaq(Faq faq) {
        return embeddingService.generateEmbedding(faq.getPergunta())
                .flatMap(vector -> {
                    Map<String, Object> payload = Map.of(
                            "faqId", faq.getId(),
                            "pergunta", faq.getPergunta()
                    );
                    return vectorStoreService.upsert(faq.getId(), vector, payload);
                })
                .doOnSuccess(v -> log.info("FAQ {} indexada com sucesso", faq.getId()))
                .doOnError(e -> log.warn("Erro ao indexar FAQ {}: {}", faq.getId(), e.getMessage()))
                .onErrorComplete()
                .then();
    }
}
