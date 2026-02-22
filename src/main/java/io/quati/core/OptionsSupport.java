package io.quati.core;

import java.util.Set;

public interface OptionsSupport {

    String option();

    Set<String> longOptions();

    default boolean is(String name) {
        return option().equals(name)
                || longOptions().contains(name);
    }

    default boolean startsWith(String partialName) {
        return option().startsWith(partialName)
                || longOptions().stream().anyMatch(option -> option.startsWith(partialName));
    }
}
