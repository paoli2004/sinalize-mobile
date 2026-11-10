package br.com.sinalize.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Usado em atualizações parciais (PATCH): o campo pode ser omitido ({@code null}),
 * mas, se for enviado, não pode estar em branco.
 */
@Documented
@Constraint(validatedBy = NaoVazioSePresente.Validador.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface NaoVazioSePresente {

    String message() default "O campo não pode estar em branco.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validador implements ConstraintValidator<NaoVazioSePresente, String> {

        @Override
        public boolean isValid(String valor, ConstraintValidatorContext context) {
            return valor == null || !valor.isBlank();
        }
    }
}
