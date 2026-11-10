package br.com.sinalize.api.dto.categoria;

import br.com.sinalize.api.entity.Categoria;

/** Dados mínimos de uma categoria, usados dentro de outras respostas (ex.: categorias de uma palavra). */
public record CategoriaResumoResponse(Long id, String nome) {

    public static CategoriaResumoResponse from(Categoria categoria) {
        return new CategoriaResumoResponse(categoria.getId(), categoria.getNome());
    }
}
