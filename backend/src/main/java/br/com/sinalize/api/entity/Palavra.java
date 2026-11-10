package br.com.sinalize.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import org.hibernate.Hibernate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "palavras")
public class Palavra extends EntidadeAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String palavra;

    @ManyToMany
    @JoinTable(
            name = "palavras_categorias",
            joinColumns = @JoinColumn(name = "palavra_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new HashSet<>();

    protected Palavra() {
        // exigido pelo JPA
    }

    public Palavra(String palavra, Collection<Categoria> categorias, Usuario criadoPor) {
        super(criadoPor);
        this.palavra = palavra;
        this.categorias.addAll(categorias);
    }

    public Long getId() {
        return id;
    }

    public String getPalavra() {
        return palavra;
    }

    public void setPalavra(String palavra) {
        this.palavra = palavra;
    }

    public Set<Categoria> getCategorias() {
        return Set.copyOf(categorias);
    }

    /** Substitui todas as categorias associadas à palavra. */
    public void substituirCategorias(Collection<Categoria> novasCategorias) {
        categorias.clear();
        categorias.addAll(novasCategorias);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Palavra outra = (Palavra) o;
        return id != null && Objects.equals(id, outra.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
