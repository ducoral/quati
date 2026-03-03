package io.quati.util;

import org.jline.reader.Candidate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Utils {

    public static <T> List<Map<?, ?>> toListOfMap(List<T> fromList, Function<T, Map<?,?>> mapper) {
        var toList = new ArrayList<Map<?, ?>>();
        fromList.forEach(item -> toList.add(mapper.apply(item)));
        return toList;
    }

    public static <T> List<T> getAsListOf(Map<?, ?> map, String key, Function<Map<?, ?>, T> mapper) {
        return ((List<?>) map.get(key))
                .stream()
                .map(column -> mapper.apply((Map<?, ?>) column))
                .toList();
    }

    public static Candidate candidate(String name) {
        return candidate(name, name, null);
    }

    public static Candidate candidate(String name, String description) {
        return candidate(name, name, description);
    }

    public static Candidate candidate(String name, String display, String description) {
        display = name.equals(display)
                ? name
                : name + " " + display;
        return new Candidate(name, display, null, description, null, null, true);
    }

    public static void completeArg(List<String> reference, String value, List<String> arguments, List<Candidate> candidates) {
        if (reference.contains(value))
            return;
        reference
                .stream()
                .filter(name -> arguments == null || !arguments.contains(name))
                .map(Utils::candidate)
                .forEach(candidates::add);
    }

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

    public static String justifyLeft(String text, int length) {
        return text.concat(diff(text, length));
    }

    public static String justifyRight(String text, int length) {
        return diff(text, length).concat(text);
    }

    private static String diff(String str, int length) {
        return length <= str.length()
                ? ""
                : " ".repeat(length - str.length());
    }
}
