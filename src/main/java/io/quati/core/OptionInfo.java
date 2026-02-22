package io.quati.core;

import io.quati.api.Arity;
import io.quati.api.Option;
import io.quati.util.Strs;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public record OptionInfo(
        Set<String> names,
        String label,
        String desc,
        Arity arity,
        Field field) {

    public static OptionInfo of(Field field) {
        var opt = field.getAnnotation(Option.class);
        return new OptionInfo(Strs.splitNames(opt.name()), opt.label(), opt.desc(), Arity.of(opt.arity()), field);
    }

    public void put(Map<String, OptionInfo> map) {
        for (var name : names)
            map.put(name, this);
    }
}
