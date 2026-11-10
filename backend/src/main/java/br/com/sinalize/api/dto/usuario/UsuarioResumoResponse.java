package br.com.sinalize.api.dto.usuario;

import br.com.sinalize.api.entity.Usuario;

/** Dados mínimos de um usuário, usados para indicar autoria (criado_por / atualizado_por). */
public record UsuarioResumoResponse(Long id, String nome) {

    /** Retorna {@code null} quando não há usuário (por exemplo, registro nunca atualizado). */
    public static UsuarioResumoResponse from(Usuario usuario) {
        return usuario == null ? null : new UsuarioResumoResponse(usuario.getId(), usuario.getNome());
    }
}
