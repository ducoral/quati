package io.quati.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {
    String name();

    String desc();

    String label() default "VALUE";

    String arity() default Arity.ZERO_OR_MORE;
}
