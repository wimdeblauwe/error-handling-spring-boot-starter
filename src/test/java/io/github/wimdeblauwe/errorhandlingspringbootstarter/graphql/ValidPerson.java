package io.github.wimdeblauwe.errorhandlingspringbootstarter.graphql;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PersonValidator.class)
public @interface ValidPerson {
    String message() default "Invalid person";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
