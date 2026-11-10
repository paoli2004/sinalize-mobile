package br.com.sinalize.api.dto.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo de {@code POST /categorias}. */
public record CategoriaRequest(

        @NotBlank(message = "O campo nome é obrigatório.")
        @Size(max = 255, message = "O campo nome deve conter no máximo 255 caracteres.")
        String nome,

        @NotBlank(message = "O campo descricao é obrigatório.")
        @Size(max = 400, message = "O campo descricao deve conter no máximo 400 caracteres.")
        String descricao) {
}
