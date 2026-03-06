package io.quati.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Json {

    public static class Obj {
        final Map<String, Object> map = new LinkedHashMap<>();

        public Obj put(String key, Object value) {
            map.put(key, value);
            return this;
        }

        @Override
        public String toString() {
            return Json.toStr(map);
        }
    }

    public static class Arr {
        final List<Object> list = new ArrayList<>();

        public Arr add(Object element) {
            list.add(element);
            return this;
        }

        @Override
        public String toString() {
            return Json.toStr(list);
        }
    }

    public static Obj object() {
        return new Obj();
    }

    public static Arr array() {
        return new Arr();
    }

    public static String toStr(Object json) {
        if (json instanceof Obj obj)
            return toStr(obj.map);
        else if (json instanceof Arr arr)
            return toStr(arr.list);
        else if (json instanceof Map<?, ?> map)
            return Join
                    .of("{", ",", "}")
                    .join(map.entrySet(), entry ->
                            "\"%s\":%s".formatted(entry.getKey(), toStr(entry.getValue())));
        else if (json instanceof List<?> list)
            return Join
                    .of("[", ",", "]")
                    .join(list, Json::toStr);
        else if (json instanceof String str)
            return "\"%s\"".formatted(str);
        else if (json instanceof Number
                || json instanceof Boolean
                || json == null)
            return String.valueOf(json);
        else
            throw new RuntimeException("invalid Json type: " + json);
    }

    public static Object parse(String json) {
        return parse(new Scan(json));
    }

    private static Object parse(Scan scan) {
        scan.skipSpaces();
        if (scan.is('{'))
            return parseObject(scan);

        if (scan.is('['))
            return parseArray(scan);

        if (scan.isStringStart())
            return scan.parseString();

        if (scan.is('-') || scan.isDigit())
            return parseNumber(scan);

        if (scan.isOneOf('t', 'f', 'n'))
            return parseLiteral(scan);

        throw new RuntimeException("character invalid: " + scan.current());
    }

    private static Object parseObject(Scan scan) {
        var map = new LinkedHashMap<>();
        scan
                .accept('{')
                .skipSpaces();
        while (scan.hasChar() && scan.notIs('}')) {
            var key = scan.parseString();
            scan
                    .skipSpaces()
                    .accept(':');
            map.put(key, parse(scan));
            scan.skipSpacesCharSpaces(',');
        }
        scan.accept('}');
        return map;
    }

    private static Object parseArray(Scan scan) {
        var list = new ArrayList<>();
        scan
                .accept('[')
                .skipSpaces();
        while (scan.hasChar() && scan.notIs(']')) {
            list.add(parse(scan));
            scan.skipSpacesCharSpaces(',');
        }
        scan.accept(']');
        return list;
    }

    private static Object parseNumber(Scan scan) {
        var lexeme = new StringBuilder();
        lexeme.append(scan.currentAndNext());
        scan.whileFor(Character::isDigit, lexeme::append);
        if (scan.is('.')) {
            lexeme.append(scan.currentAndNext());
            scan.whileFor(Character::isDigit, lexeme::append);
            if (scan.isOneOf('e', 'E')) {
                lexeme.append(scan.currentAndNext());
                if (scan.isOneOf('-', '+'))
                    lexeme.append(scan.currentAndNext());
                scan.whileFor(Character::isDigit, lexeme::append);
            }
            return Double.parseDouble(lexeme.toString());
        }
        return Integer.parseInt(lexeme.toString());
    }

    private static Object parseLiteral(Scan scan) {
        if (scan.is('t')) {
            scan.accept("true");
            return Boolean.TRUE;
        }
        if (scan.is('f')) {
            scan.accept("false");
            return Boolean.FALSE;
        }
        if (scan.is('n')) {
            scan.accept("null");
            return null;
        }
        throw new RuntimeException("character invalid: " + scan.current());
    }
}