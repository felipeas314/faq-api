package br.com.example.dto.response;

import br.com.example.model.Faq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaqResponse {

    private String id;
    private String pergunta;
    private String resposta;
    private String categoria;
    private List<String> tags;
    private String criadoPor;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public static FaqResponse fromEntity(Faq faq) {
        return FaqResponse.builder()
                .id(faq.getId())
                .pergunta(faq.getPergunta())
                .resposta(faq.getResposta())
                .categoria(faq.getCategoria())
                .tags(faq.getTags())
                .criadoPor(faq.getCriadoPor())
                .criadoEm(faq.getCriadoEm())
                .atualizadoEm(faq.getAtualizadoEm())
                .build();
    }
}
