package io.quati.core;

import io.quati.util.Scan;

public enum AnsiColor {
    BLACK(30),
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    PURPLE(35),
    CYAN(36),
    WHITE(37);

    public String fg(String str) {
        return fg(str, false);
    }

    public String fg(String str, boolean bright) {
        var color = bright
                ? code + BRIGHT_OFFSET
                : code;
        return TEMPLATE.formatted(color, str, RESET_FG);
    }

    public String bg(String str) {
        return bg(str, false);
    }

    public String bg(String str, boolean bright) {
        var color = bright
                ? code + BACKGROUND_OFFSET + BRIGHT_OFFSET
                : code + BACKGROUND_OFFSET;
        return TEMPLATE.formatted(color, str, RESET_BG);
    }

    public static String filter(String str) {
        var builder = new StringBuilder();
        var scan = new Scan(str);
        while (!scan.isEof()) {
            if (scan.is(':')) {
                scan.next();
                if (scan.is(':')) {
                    scan.next();
                    builder.append(ESCAPE.formatted(RESET));
                    continue;
                }
                if ("krgybpcwz".indexOf(Character.toLowerCase(scan.current())) > -1) {
                    var token = scan.currentAndNext();
                    if (scan.is(':')) {
                        scan.next();
                        builder.append(ESCAPE.formatted(colorCode(token, false)));
                        continue;
                    }
                    if (scan.is(token)) {
                        scan.next();
                        if (scan.is(':')) {
                            scan.next();
                            builder.append(ESCAPE.formatted(colorCode(token, true)));
                            continue;
                        }
                        builder.append(':').append(token).append(token);
                    }
                    builder.append(':').append(token);
                }
                builder.append(':');
            }
            builder.append(scan.currentAndNext());
        }
        return builder.toString();
    }

    private static int colorCode(char token, boolean bright) {
        var tokenLower = Character.toLowerCase(token);
        if (tokenLower == 'z')
            return Character.isLowerCase(token)
                    ? RESET_FG
                    : RESET_BG;
        var colorCode = switch (tokenLower) {
            case 'k' -> BLACK.code;
            case 'r' -> RED.code;
            case 'g' -> GREEN.code;
            case 'y' -> YELLOW.code;
            case 'b' -> BLUE.code;
            case 'p' -> PURPLE.code;
            case 'c' -> CYAN.code;
            case 'w' -> WHITE.code;
            default -> throw new InternalError("Token invalid '%s'".formatted(token));
        };
        if (bright)
            colorCode += BRIGHT_OFFSET;
        if (Character.isUpperCase(token))
            colorCode += BACKGROUND_OFFSET;
        return colorCode;
    }

    private static final int RESET = 0;
    private static final int RESET_FG = 39;
    private static final int RESET_BG = 49;

    private static final int BACKGROUND_OFFSET = 10;
    private static final int BRIGHT_OFFSET = 60;

    private static final String ESCAPE = "\u001B[%dm";
    private static final String TEMPLATE = "\u001B[%dm%s\u001B[%dm";

    private final int code;

    AnsiColor(int code) {
        this.code = code;
    }
}