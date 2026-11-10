package br.com.sinalize.api.dto.categoria;

import br.com.sinalize.api.validation.NaoVazioSePresente;
import jakarta.validation.constraints.Size;

/** Corpo de {@code PATCH /categorias/{id}}. Campos omitidos não são alterados. */
public record CategoriaAtualizacaoRequest(

        @NaoVazioSePresente(message = "O campo nome não pode estar em branco.")
        @Size(max = 255, message = "O campo nome deve conter no máximo 255 caracteres.")
        String nome,

        @NaoVazioSePresente(message = "O campo descricao não pode estar em branco.")
        @Size(max = 400, message = "O campo descricao deve conter no máximo 400 caracteres.")
        String descricao) {
}
