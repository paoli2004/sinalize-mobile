package br.com.sinalize.api.service;

import br.com.sinalize.api.entity.Usuario;

/**
 * Informa qual usuário está realizando a operação, para registrar a autoria
 * (criado_por / atualizado_por) e o dono dos quizzes.
 *
 * <p>O projeto original ainda não tem autenticação e usa sempre o usuário 1. Isolar essa
 * decisão nesta interface permite trocar a implementação por uma baseada em login
 * sem alterar os serviços.</p>
 */
public interface UsuarioLogadoProvider {

    Usuario obter();
}
