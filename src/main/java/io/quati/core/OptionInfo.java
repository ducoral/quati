package io.quati.core;

import io.quati.api.Arity;
import io.quati.api.Option;
import io.quati.util.Strs;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public record OptionInfo(
        String option,
        Set<String> longOptions,
        String label,
        String desc,
        Arity arity,
        Field field) implements OptionsSupport {

    public static OptionInfo of(Field field) {
        var opt = field.getAnnotation(Option.class);
        var names = Strs.splitNames(opt.name());
        var longOptions = Set.of(Strs.tail(names));
        var arity = Arity.of(opt.arity());
        return new OptionInfo(names[0], longOptions, opt.label(), opt.desc(), arity, field);
    }

    public void put(Map<String, OptionInfo> map) {
        map.put(option(), this);
        for (var name : longOptions())
            map.put(name, this);
    }
}
