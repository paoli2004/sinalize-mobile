package br.com.sinalize.api.dto.usuario;

import br.com.sinalize.api.validation.SenhaForte;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpo de {@code POST /usuarios}. */
public record UsuarioRequest(

        @NotBlank(message = "O campo nome é obrigatório.")
        @Size(max = 255, message = "O campo nome deve conter no máximo 255 caracteres.")
        String nome,

        @NotBlank(message = "O campo email é obrigatório.")
        @Email(message = "O e-mail informado é inválido.")
        @Size(max = 255, message = "O campo email deve conter no máximo 255 caracteres.")
        String email,

        @NotBlank(message = "O campo senha é obrigatório.")
        @SenhaForte
        String senha) {

    /** Evita que a senha apareça em logs caso o objeto seja impresso. */
    @Override
    public String toString() {
        return "UsuarioRequest[nome=" + nome + ", email=" + email + ", senha=***]";
    }
}
