package br.com.sinalize.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Campos de auditoria compartilhados por {@link Categoria} e {@link Palavra}:
 * quem criou e quando, e quem fez a última alteração e quando.
 */
@MappedSuperclass
public abstract class EntidadeAuditavel {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criado_por", nullable = false, updatable = false)
    private Usuario criadoPor;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atualizado_por")
    private Usuario atualizadoPor;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    protected EntidadeAuditavel() {
    }

    protected EntidadeAuditavel(Usuario criadoPor) {
        this.criadoPor = criadoPor;
    }

    /** Registra quem fez a alteração e quando. Deve ser chamado a cada atualização. */
    public void registrarAtualizacao(Usuario usuario) {
        this.atualizadoPor = usuario;
        this.atualizadoEm = LocalDateTime.now();
    }

    public Usuario getCriadoPor() {
        return criadoPor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public Usuario getAtualizadoPor() {
        return atualizadoPor;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
