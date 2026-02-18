package io.quati.core;

import java.util.regex.Pattern;

public class ColorFilter {

    static final String RESET = "\u001B[0m";

    enum Color {
        BLACK("bk", "\u001B[30m"),
        RED("r", "\u001B[31m"),
        GREEN("g", "\u001B[32m"),
        YELLOW("y", "\u001B[33m"),
        BLUE("b", "\u001B[34m"),
        PURPLE("p", "\u001B[35m"),
        CYAN("c", "\u001B[36m"),
        WHITE("w", "\u001B[37m"),
        BLACK_BRIGHT  ("bkB",  "\u001B[90m"),
        RED_BRIGHT    ("rB",  "\u001B[91m"),
        GREEN_BRIGHT  ("gB",  "\u001B[92m"),
        YELLOW_BRIGHT ("yB",  "\u001B[93m"),
        BLUE_BRIGHT   ("bB",  "\u001B[94m"),
        PURPLE_BRIGHT ("pB",  "\u001B[95m"),
        CYAN_BRIGHT   ("cB",  "\u001B[96m"),
        WHITE_BRIGHT  ("wB",  "\u001B[97m");

        final Pattern pattern;
        final String code;

        String apply(String text) {
            return code + text + RESET;
        }

        Color(String prefix, String code) {
            pattern = Pattern.compile(prefix + "\\{([^}]+)}");
            this.code = code;
        }
    }

    public static String apply(String message) {
        for (var filter : Color.values()) {
            var match = filter.pattern.matcher(message);
            while (match.find()) {
                message = message.replace(match.group(), filter.apply(match.group(1)));
                match = filter.pattern.matcher(message);
            }
        }
        return message;
    }
}