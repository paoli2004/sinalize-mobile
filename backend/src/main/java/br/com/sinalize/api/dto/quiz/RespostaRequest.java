package br.com.sinalize.api.dto.quiz;

import jakarta.validation.constraints.NotNull;

/** Corpo de {@code PATCH /quizzes/palavras/{id}/responder}. */
public record RespostaRequest(

        @NotNull(message = "O campo acertou é obrigatório.")
        Boolean acertou) {
}
