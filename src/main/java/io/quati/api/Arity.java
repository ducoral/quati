package io.quati.api;

import java.util.regex.Pattern;

public interface Arity {
    String ONE = "1";
    String TWO = "2";
    String ZERO_OR_ONE = "0..1";
    String ZERO_OR_MORE = "0..*";
    String ONE_OR_MORE = "1..*";
    String TWO_OR_MORE = "2..*";

    int min();

    int max();

    default boolean isFixed() {
        return min() == max();
    }

    default boolean hasMax() {
        return max() != 1000;
    }

    default boolean hasPosition(int position) {
        return position <= max();
    }

    static Arity of(String arity) {
        var match = Pattern
                .compile("^(\\d+)(\\.\\.(\\d+|\\*))?$")
                .matcher(arity);
        if (!match.find())
            throw new RuntimeException("Invalid arity format: " + arity);
        var min = Integer.parseInt(match.group(1));
        var max = match.group(2) == null
                ? min
                : (match.group(3).equals("*") ? 1000 : Integer.parseInt(match.group(3)));
        return new Arity() {
            @Override
            public int min() {
                return min;
            }

            @Override
            public int max() {
                return max;
            }

            @Override
            public String toString() {
                return "[%s]".formatted(arity);
            }
        };
    }
}
