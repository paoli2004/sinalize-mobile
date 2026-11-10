package br.com.sinalize.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.Objects;

/** Uma palavra sorteada para um quiz, com a resposta do jogador. */
@Entity
@Table(name = "quiz_palavras")
public class QuizPalavra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_quiz", nullable = false, updatable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_palavra", nullable = false, updatable = false)
    private Palavra palavra;

    /** {@code null} enquanto a palavra não foi respondida. */
    @Column(name = "acertou")
    private Boolean acertou;

    @Column(name = "respondido_em")
    private LocalDateTime respondidoEm;

    protected QuizPalavra() {
        // exigido pelo JPA
    }

    QuizPalavra(Quiz quiz, Palavra palavra) {
        this.quiz = quiz;
        this.palavra = palavra;
    }

    /** Registra a resposta do jogador. Responder de novo sobrescreve a resposta anterior. */
    public void responder(boolean acertou) {
        this.acertou = acertou;
        this.respondidoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public Palavra getPalavra() {
        return palavra;
    }

    public Boolean getAcertou() {
        return acertou;
    }

    public LocalDateTime getRespondidoEm() {
        return respondidoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        QuizPalavra outra = (QuizPalavra) o;
        return id != null && Objects.equals(id, outra.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
