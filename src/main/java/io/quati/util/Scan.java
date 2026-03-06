package io.quati.util;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Scan {

    final String text;

    int pos = -1;

    public Scan(String text) {
        this.text = text;
        next();
    }

    public char current() {
        if (isEof())
            return '\0';
        return text.charAt(pos);
    }

    public char currentAndNext() {
        var ch = (char) current();
        next();
        return ch;
    }

    public boolean is(char ch) {
        return ch == current();
    }

    public boolean isIgnoreCase(char ch) {
        return Character.toLowerCase(ch) == Character.toLowerCase(current());
    }

    public boolean notIs(char ch) {
        return !is(ch);
    }

    public boolean isOneOf(char... chars) {
        for (char c : chars)
            if (c == current())
                return true;
        return false;
    }

    public boolean isOneOfIgnoreCase(char... chars) {
        for (char c : chars)
            if (Character.toLowerCase(c) == Character.toLowerCase(current()))
                return true;
        return false;
    }

    public boolean isDigit() {
        return Character.isDigit(current());
    }

    public boolean isLetter() {
        return Character.isLetter(current());
    }

    public boolean isLetterOrDigit() {
        return Character.isLetterOrDigit(current());
    }

    public boolean isHexDigit() {
        return isDigit()
                || isOneOfIgnoreCase('a', 'b', 'c', 'd', 'e', 'f');
    }

    public boolean isIdentifier() {
        return isLetterOrDigit()
                || isOneOf('-', '_');
    }

    public boolean isStringStart() {
        return current() == '"';
    }

    public boolean isSpace() {
        return Character.isWhitespace(current());
    }

    public boolean notIsSpace() {
        return !isSpace();
    }

    public Scan skipSpaces() {
        while (hasChar() && isSpace())
            next();
        return this;
    }

    public Scan skipSpacesCharSpaces(char ch) {
        skipSpaces()
                .acceptOpt(ch)
                .skipSpaces();
        return this;
    }

    public static Predicate<Character> condEq(char ch) {
        return test -> test == ch;
    }

    public static Predicate<Character> condNotEq(char ch) {
        return condEq(ch).negate();
    }

    public static Predicate<Character> condSpace() {
        return Character::isWhitespace;
    }

    public static Predicate<Character> condNotSpace() {
        return condSpace().negate();
    }

    public Scan whileFor(Predicate<Character> condition, Consumer<Character> action) {
        while (hasChar() && condition.test(current())) {
            if (is('\\'))
                next();
            action.accept(current());
            next();
        }
        return this;
    }

    public String parseString() {
        var string = new StringBuilder();
        accept('"')
                .whileFor(Scan.condNotEq('"'), string::append)
                .accept('"');
        return string.toString();
    }

    public Scan accept(char ch) {
        if (ch != current())
            throw new RuntimeException(
                    "the character '%s' was expected, but '%s' was found".formatted(current(), ch));
        next();
        return this;
    }

    public Scan accept(String str) {
        for (var index = 0; index < str.length(); index++)
            acceptOpt(str.charAt(index));
        return this;
    }

    public Scan acceptOpt(char ch) {
        if (ch == current())
            next();
        return this;
    }

    public Scan next() {
        if (isEof())
            return this;
        pos++;
        return this;
    }

    public boolean hasChar() {
        return !isEof();
    }

    public boolean isEof() {
        return pos == text.length();
    }
}