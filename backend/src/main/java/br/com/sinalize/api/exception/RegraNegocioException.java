package br.com.sinalize.api.exception;

/**
 * Lançada quando a requisição é válida, mas não pode ser atendida por uma regra de negócio
 * (por exemplo, criar um quiz com categorias que não têm palavras). Resulta em HTTP 422.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
