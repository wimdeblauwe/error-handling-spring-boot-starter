package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseErrorProperty {
    String value() default "";

    boolean includeIfNull() default false;
}
