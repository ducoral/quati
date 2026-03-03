package io.quati.core;

import io.quati.api.Argument;
import io.quati.api.Arity;

import java.lang.reflect.Field;

public record ArgumentInfo(String label, String desc, Arity arity, Field field) {
    public static ArgumentInfo of(Field field) {
        var arg = field.getAnnotation(Argument.class);
        return new ArgumentInfo(arg.label(), arg.description(), Arity.of(arg.arity()), field);
    }

    public boolean hasPosition(int position) {
        return arity.hasPosition(position);
    }
}
