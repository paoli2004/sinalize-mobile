package br.com.sinalize.api.dto.usuario;

import br.com.sinalize.api.entity.Usuario;

import java.time.LocalDateTime;

/** Dados públicos de um usuário. A senha (nem o hash) nunca é devolvida pela API. */
public record UsuarioResponse(Long id, String nome, String email, LocalDateTime criadoEm) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getCriadoEm());
    }
}
