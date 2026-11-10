package br.com.sinalize.api.dto.categoria;

import br.com.sinalize.api.dto.usuario.UsuarioResumoResponse;
import br.com.sinalize.api.entity.Categoria;

import java.time.LocalDateTime;

public record CategoriaResponse(
        Long id,
        String nome,
        String descricao,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        UsuarioResumoResponse criadoPor,
        UsuarioResumoResponse atualizadoPor) {

    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getCriadoEm(),
                categoria.getAtualizadoEm(),
                UsuarioResumoResponse.from(categoria.getCriadoPor()),
                UsuarioResumoResponse.from(categoria.getAtualizadoPor()));
    }
}
