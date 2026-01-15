package br.com.example.controller;

import br.com.example.dto.request.FaqRequest;
import br.com.example.dto.request.SearchRequest;
import br.com.example.dto.response.FaqResponse;
import br.com.example.dto.response.SearchResponse;
import br.com.example.service.FaqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FaqResponse> create(
            @Valid @RequestBody FaqRequest request,
            @AuthenticationPrincipal String username) {
        return faqService.create(request, username);
    }

    @GetMapping
    public Flux<FaqResponse> findAll() {
        return faqService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<FaqResponse> findById(@PathVariable String id) {
        return faqService.findById(id);
    }

    @PutMapping("/{id}")
    public Mono<FaqResponse> update(
            @PathVariable String id,
            @Valid @RequestBody FaqRequest request,
            @AuthenticationPrincipal String username) {
        return faqService.update(id, request, username);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return faqService.delete(id);
    }

    @PostMapping("/search")
    public Flux<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        return faqService.search(request);
    }
}
