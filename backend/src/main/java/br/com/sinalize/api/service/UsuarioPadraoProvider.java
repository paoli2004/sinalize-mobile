package br.com.sinalize.api.service;

import br.com.sinalize.api.entity.Usuario;
import br.com.sinalize.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Implementação sem autenticação: considera sempre o usuário padrão como logado. */
@Component
public class UsuarioPadraoProvider implements UsuarioLogadoProvider {

    private final UsuarioRepository usuarioRepository;
    private final Long usuarioPadraoId;

    public UsuarioPadraoProvider(UsuarioRepository usuarioRepository,
                                 @Value("${sinalize.usuario-padrao-id:1}") Long usuarioPadraoId) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioPadraoId = usuarioPadraoId;
    }

    @Override
    public Usuario obter() {
        return usuarioRepository.findById(usuarioPadraoId)
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário padrão (id " + usuarioPadraoId + ") não encontrado. Verifique os dados iniciais."));
    }
}
