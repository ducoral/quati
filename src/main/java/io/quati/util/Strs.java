package io.quati.util;

import java.util.Set;

public class Strs {

    public static Set<String> splitNames(String names) {
        names = names.replaceAll("\\s+", "");
        if (names.startsWith("|"))
            names = names.substring(1);
        if (names.endsWith("|"))
            names = names.substring(0, names.length() - 1);
        return Set.of(names.strip().split("\\|"));
    }
}
