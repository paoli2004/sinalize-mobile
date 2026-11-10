package br.com.sinalize.api.exception;

/**
 * Lançada quando a operação conflita com o estado atual dos dados
 * (por exemplo, e-mail já cadastrado). Resulta em HTTP 409.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
