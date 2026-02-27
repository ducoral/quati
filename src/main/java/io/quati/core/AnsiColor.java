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

    public String fg(String text) {
        return fg(text, false, AnsiStyle.NORMAL);
    }

    public String fg(String text, AnsiStyle style) {
        return fg(text, false, style);
    }

    public String fg(String text, boolean bright) {
        return fg(text, bright, AnsiStyle.NORMAL);
    }

    public String fg(String text, boolean bright, AnsiStyle style) {
        var color = bright
                ? code + BRIGHT_OFFSET
                : code;
        var styleStart = "";
        var styleEnd = "";
        if (style == AnsiStyle.BOLD || style == AnsiStyle.BOLD_ITALIC) {
            styleStart = escape(BOLD);
            styleEnd = escape(RESET_BOLD);
        }
        if (style == AnsiStyle.ITALIC || style == AnsiStyle.BOLD_ITALIC) {
            styleStart += escape(ITALIC);
            styleEnd += escape(RESET_ITALIC);
        }
        return escape(color)
                + styleStart
                + text
                + styleEnd
                + escape(RESET_FG);
    }

    public static String fg(String text, String hexRGB) {
        return escapeFgColor(hexRGB)
                + text
                + escape(RESET_FG);
    }

    public String bg(String text) {
        return bg(text, false);
    }

    public String bg(String text, boolean bright) {
        var color = bright
                ? code + BACKGROUND_OFFSET + BRIGHT_OFFSET
                : code + BACKGROUND_OFFSET;
        return escape(color)
                + text
                + escape(RESET_BG);
    }

    public static String bg(String text, String hexRGB) {
        return escapeBgColor(hexRGB)
                + text
                + escape(RESET_BG);
    }

    public static String filter(String text) {
        var builder = new StringBuilder(text);
        int from = 0, start, end;
        while ((start = builder.indexOf("`", from)) > -1) {
            if ((end = builder.indexOf("`", start + 1) + 1) > 1) {
                var style = parse(builder.substring(start, end));
                builder.replace(start, end, style);
                from = start + style.length();
            } else
                from = start;
        }
        return builder.toString();
    }

    private static String parse(String style) {
        var builder = new StringBuilder();
        var scan = new Scan(style.substring(1, style.length() - 1));
        if (scan.is(':')) {
            scan.accept(':');
            if (scan.isEof())
                builder.append(escape(RESET));
            else while (!scan.isEof()) {
                if (scan.is('z'))
                    builder.append(escape(RESET_FG));
                else if (scan.is('Z'))
                    builder.append(escape(RESET_BG));
                else if (scan.is('*'))
                    builder.append(escape(RESET_BOLD));
                else if (scan.is('_'))
                    builder.append(escape(RESET_ITALIC));
                else
                    return style;
                scan.next();
            }
        } else while (!scan.isEof()) {
            if (scan.isOneOfIgnoreCase('k', 'r', 'g', 'y', 'b', 'p', 'c', 'w')) {
                var token = scan.currentAndNext();
                if (scan.is(token))
                    builder.append(escape(colorCode(scan.currentAndNext(), true)));
                else
                    builder.append(escape(colorCode(token, false)));
            } else if (scan.is('*')) {
                builder.append(escape(BOLD));
                scan.next();
            } else if (scan.is('_')) {
                builder.append(escape(ITALIC));
                scan.next();
            } else if (scan.isIgnoreCase('x')) {
                var isBg = Character.isUpperCase(scan.currentAndNext());
                var hex = new StringBuilder("#");
                while (!scan.isEof()
                        && hex.length() < 7
                        && scan.isHexDigit()) {
                    hex.append(scan.currentAndNext());
                }
                if (hex.length() != 7)
                    return style;
                var code = isBg
                        ? escapeBgColor(hex.toString())
                        : escapeFgColor(hex.toString());
                builder.append(code);
            } else
                return style;
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
            default -> throw new InternalError("Token invalid '%s'.".formatted(token));
        };
        if (bright)
            colorCode += BRIGHT_OFFSET;
        if (Character.isUpperCase(token))
            colorCode += BACKGROUND_OFFSET;
        return colorCode;
    }

    private static final int RESET = 0;
    private static final int RESET_BOLD = 22;
    private static final int RESET_ITALIC = 23;
    private static final int RESET_FG = 39;
    private static final int RESET_BG = 49;

    private static final int BOLD = 1;
    private static final int ITALIC = 3;

    private static final int BACKGROUND_OFFSET = 10;
    private static final int BRIGHT_OFFSET = 60;

    private final int code;

    AnsiColor(int code) {
        this.code = code;
    }

    private static String escape(int code) {
        return "\033[%dm".formatted(code);
    }

    private static String escapeFgColor(String hexRGB) {
        return formatHexRGB("\033[38;2;%d;%d;%dm", hexRGB);
    }

    private static String escapeBgColor(String hexRGB) {
        return formatHexRGB("\033[48;2;%d;%d;%dm", hexRGB);
    }

    private static String formatHexRGB(String format, String hexRGB) {
        if (!hexRGB.matches("#[aAbBcCdDeEfF\\d]{6}"))
            throw new RuntimeException("Invalid HEX RGB color '%s'".formatted(hexRGB));
        return format.formatted(
                Integer.parseInt(hexRGB.substring(1, 3), 16),
                Integer.parseInt(hexRGB.substring(3, 5), 16),
                Integer.parseInt(hexRGB.substring(5, 7), 16));
    }
}