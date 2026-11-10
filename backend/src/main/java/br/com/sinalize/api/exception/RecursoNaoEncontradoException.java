package br.com.sinalize.api.exception;

/** Lançada quando o recurso solicitado não existe. Resulta em HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
