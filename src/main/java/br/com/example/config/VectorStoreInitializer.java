package br.com.example.config;

import br.com.example.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorStoreInitializer implements ApplicationRunner {

    private final VectorStoreService vectorStoreService;

    @Override
    public void run(ApplicationArguments args) {
        vectorStoreService.createCollectionIfNotExists()
                .doOnSuccess(v -> log.info("Qdrant collection verificada/criada"))
                .doOnError(e -> log.warn("Erro ao inicializar Qdrant: {}", e.getMessage()))
                .subscribe();
    }
}
