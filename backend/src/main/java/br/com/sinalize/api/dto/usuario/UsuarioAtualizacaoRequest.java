package br.com.sinalize.api.dto.usuario;

import br.com.sinalize.api.validation.NaoVazioSePresente;
import br.com.sinalize.api.validation.SenhaForte;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** Corpo de {@code PATCH /usuarios/{id}}. Campos omitidos não são alterados. */
public record UsuarioAtualizacaoRequest(

        @NaoVazioSePresente(message = "O campo nome não pode estar em branco.")
        @Size(max = 255, message = "O campo nome deve conter no máximo 255 caracteres.")
        String nome,

        @NaoVazioSePresente(message = "O campo email não pode estar em branco.")
        @Email(message = "O e-mail informado é inválido.")
        @Size(max = 255, message = "O campo email deve conter no máximo 255 caracteres.")
        String email,

        @SenhaForte
        String senha) {

    /** Evita que a senha apareça em logs caso o objeto seja impresso. */
    @Override
    public String toString() {
        return "UsuarioAtualizacaoRequest[nome=" + nome + ", email=" + email
                + ", senha=" + (senha == null ? null : "***") + "]";
    }
}
