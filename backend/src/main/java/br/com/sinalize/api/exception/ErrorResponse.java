package br.com.sinalize.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Corpo padrão de todas as respostas de erro da API.
 *
 * @param campos erros de validação por campo; só aparece em respostas 400 de validação
 */
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<CampoErro> campos) {

    public record CampoErro(String campo, String mensagem) {
    }

    public static ErrorResponse of(HttpStatus status, String mensagem, String path, List<CampoErro> campos) {
        return new ErrorResponse(OffsetDateTime.now(), status.value(), status.getReasonPhrase(),
                mensagem, path, campos);
    }
}
