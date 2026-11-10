package br.com.sinalize.api.repository;

import br.com.sinalize.api.entity.Categoria;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /** Lista as categorias já com os autores carregados (evita consultas N+1). */
    @EntityGraph(attributePaths = {"criadoPor", "atualizadoPor"})
    List<Categoria> findAllByOrderByCriadoEmAscIdAsc();

    @EntityGraph(attributePaths = {"criadoPor", "atualizadoPor"})
    Optional<Categoria> findDetalhadaById(Long id);
}
