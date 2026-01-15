package br.com.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaqRequest {

    @NotBlank(message = "Pergunta é obrigatória")
    private String pergunta;

    @NotBlank(message = "Resposta é obrigatória")
    private String resposta;

    private String categoria;

    private List<String> tags;
}
