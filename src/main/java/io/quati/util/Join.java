package io.quati.util;

import java.util.Collection;
import java.util.function.Function;

public class Join {
    final String begin;
    final String middle;
    final String end;

    private Join(String begin, String middle, String end) {
        this.begin = begin;
        this.middle = middle;
        this.end = end;
    }

    public static Join of(String begin, String middle, String end) {
        return new Join(begin, middle, end);
    }

    public static Join of(String middle) {
        return new Join("", middle, "");
    }

    public String join(Collection<String> elements) {
        return begin
                .concat(String.join(middle, elements))
                .concat(end);
    }

    public <T> String join(Collection<T> elements, Function<T, String> toStr) {
        return join(elements
                .stream()
                .map(toStr)
                .toList());
    }
}