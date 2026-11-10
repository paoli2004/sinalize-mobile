package br.com.sinalize.api.dto.palavra;

import br.com.sinalize.api.entity.Palavra;

/** Dados mínimos de uma palavra (usado nas alternativas do quiz). */
public record PalavraResumoResponse(Long id, String palavra) {

    public static PalavraResumoResponse from(Palavra palavra) {
        return new PalavraResumoResponse(palavra.getId(), palavra.getPalavra());
    }
}
