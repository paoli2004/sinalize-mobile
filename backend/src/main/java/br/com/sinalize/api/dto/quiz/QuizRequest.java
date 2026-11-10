package br.com.sinalize.api.dto.quiz;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Corpo de {@code POST /quizzes}: categorias de onde as palavras do quiz serão sorteadas. */
public record QuizRequest(

        @NotEmpty(message = "Informe pelo menos uma categoria.")
        List<@NotNull(message = "O campo categoria_ids não pode conter valores nulos.") Long> categoriaIds) {
}
