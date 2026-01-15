package br.com.example.repository;

import br.com.example.model.Faq;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface FaqRepository extends ReactiveMongoRepository<Faq, String> {

    Flux<Faq> findByCategoria(String categoria);

    Flux<Faq> findByTagsContaining(String tag);
}
