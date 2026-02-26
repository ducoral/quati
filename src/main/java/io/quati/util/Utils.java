package io.quati.util;

import org.jline.reader.Candidate;

import java.util.Arrays;

public class Utils {

    public static Candidate candidate(String name) {
        return candidate(name, name, null);
    }

    public static Candidate candidate(String name, String description) {
        return candidate(name, name, description);
    }

    public static Candidate candidate(String name, String display, String description) {
        return new Candidate(name, display, null, description, null, null, true);
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
