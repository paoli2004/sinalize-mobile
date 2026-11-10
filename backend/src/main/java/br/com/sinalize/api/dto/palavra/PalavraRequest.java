package br.com.sinalize.api.dto.palavra;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Corpo de {@code POST /palavras}. */
public record PalavraRequest(

        @NotBlank(message = "O campo palavra é obrigatório.")
        @Size(max = 255, message = "O campo palavra deve conter no máximo 255 caracteres.")
        String palavra,

        @NotEmpty(message = "A palavra deve estar associada a pelo menos uma categoria.")
        List<@NotNull(message = "O campo categoria_ids não pode conter valores nulos.") Long> categoriaIds) {
}
