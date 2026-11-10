package br.com.sinalize.api.repository;

import br.com.sinalize.api.entity.QuizPalavra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizPalavraRepository extends JpaRepository<QuizPalavra, Long> {

    /** Indica se a palavra já foi usada em algum quiz (nesse caso ela não pode ser excluída). */
    boolean existsByPalavraId(Long palavraId);

    @EntityGraph(attributePaths = "palavra")
    Optional<QuizPalavra> findComPalavraById(Long id);
}
