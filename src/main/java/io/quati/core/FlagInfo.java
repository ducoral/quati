package io.quati.core;

import io.quati.api.Flag;
import io.quati.util.Strs;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public record FlagInfo(
        String option,
        Set<String> longOptions,
        String desc,
        Field field) implements OptionsSupport {

    public static FlagInfo of(Field field) {
        var flag = field.getAnnotation(Flag.class);
        var names = Strs.splitNames(flag.name());
        var longOptions = Set.of(Strs.tail(names));
        return new FlagInfo(names[0], longOptions, flag.desc(), field);
    }

    public void put(Map<String, FlagInfo> map) {
        map.put(option(), this);
        for (var name : longOptions())
            map.put(name, this);
    }
}
