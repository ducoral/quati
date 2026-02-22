package io.quati.util;

import java.util.Arrays;

public class Strs {

    public static String[] splitNames(String names) {
        names = names.replaceAll("\\s+", "");
        if (names.startsWith("|"))
            names = names.substring(1);
        if (names.endsWith("|"))
            names = names.substring(0, names.length() - 1);
        return names.strip().split("\\|");
    }

    public static String[] tail(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}
