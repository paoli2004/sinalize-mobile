package br.com.sinalize.api.dto.palavra;

import br.com.sinalize.api.dto.categoria.CategoriaResumoResponse;
import br.com.sinalize.api.dto.usuario.UsuarioResumoResponse;
import br.com.sinalize.api.entity.Palavra;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record PalavraResponse(
        Long id,
        String palavra,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        UsuarioResumoResponse criadoPor,
        UsuarioResumoResponse atualizadoPor,
        List<CategoriaResumoResponse> categorias) {

    public static PalavraResponse from(Palavra palavra) {
        return new PalavraResponse(
                palavra.getId(),
                palavra.getPalavra(),
                palavra.getCriadoEm(),
                palavra.getAtualizadoEm(),
                UsuarioResumoResponse.from(palavra.getCriadoPor()),
                UsuarioResumoResponse.from(palavra.getAtualizadoPor()),
                palavra.getCategorias().stream()
                        .map(CategoriaResumoResponse::from)
                        .sorted(Comparator.comparing(CategoriaResumoResponse::nome))
                        .toList());
    }
}
