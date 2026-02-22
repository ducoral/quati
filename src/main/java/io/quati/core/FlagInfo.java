package io.quati.core;

import io.quati.api.Flag;
import io.quati.util.Strs;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public record FlagInfo(
        Set<String> names,
        String desc,
        Field field) {

    public static FlagInfo of(Field field) {
        var flag = field.getAnnotation(Flag.class);
        return new FlagInfo(Strs.splitNames(flag.name()), flag.desc(), field);
    }

    public void put(Map<String, FlagInfo> map) {
        for (var name : names)
            map.put(name, this);
    }
}
