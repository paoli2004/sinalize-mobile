package br.com.sinalize.api.repository;

import br.com.sinalize.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByCriadoEmAscIdAsc();

    boolean existsByEmailIgnoreCase(String email);

    /** Verifica se o e-mail já pertence a outro usuário (usado na atualização). */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    /**
     * Indica se o usuário criou categorias ou palavras ou jogou algum quiz.
     * Nesses casos ele não pode ser excluído (chaves estrangeiras com RESTRICT).
     */
    @Query("""
            SELECT CASE WHEN
                   EXISTS (SELECT 1 FROM Categoria c WHERE c.criadoPor.id = :id)
                OR EXISTS (SELECT 1 FROM Palavra p WHERE p.criadoPor.id = :id)
                OR EXISTS (SELECT 1 FROM Quiz q WHERE q.usuario.id = :id)
            THEN true ELSE false END
            FROM Usuario u WHERE u.id = :id
            """)
    boolean possuiRegistrosAssociados(@Param("id") Long id);
}
