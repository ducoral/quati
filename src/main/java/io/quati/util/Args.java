package io.quati.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <pre><code>
 * args     ::= ( name | optional )*
 *
 * optional ::= '-' '-'? name ( assign )?
 *
 * assign   ::= '=' ( value | string )
 *
 * value    ::= ( ANY_NON_SPACE_CHARACTER )*
 *
 * string   ::= '"' ( ANY_CHARACTER )* '"'
 *
 * name     ::= LETTER ( LETTER | DIGIT | '-' | '_' )*
 *
 * </code></pre>
 */
public class Args {

    public record Cmd(String name, int index) {
    }

    public record Opt(String name, String value) {
    }

    public final List<Cmd> cmds = new ArrayList<>();

    public final Set<Opt> opts = new HashSet<>();

    public Args addCmd(String name) {
        cmds.add(new Cmd(name, cmds.size()));
        return this;
    }

    public Args addOpt(String name, String value) {
        opts.add(new Opt(name, value));
        return this;
    }

    public static Args parse(String[] parameters) {
        var input = new StringBuilder();
        for (var param : parameters)
            input
                    .append(String.valueOf(param).trim())
                    .append(' ');
        return parse(input.toString());
    }

    public static Args parse(String input) {
        var args = new Args();
        parse(args, new Scan(input));
        return args;
    }

    private static void parse(Args args, Scan scan) {
        while (scan.hasChar()) {
            scan.skipSpaces();
            if (scan.isLetter())
                args.addCmd(parseName(scan));
            else if (scan.is('-'))
                parseOpt(args, scan);
            else
                throw new RuntimeException("Invalid character: " + scan.current());
        }
    }

    private static void parseOpt(Args args, Scan scan) {
        var name = parseName(scan);
        var value = "";
        if (scan.is('=')) {
            scan.accept('=');
            value = scan.isStringStart()
                    ? scan.parseString()
                    : parseValue(scan);
        }
        args.addOpt(name, value);
    }

    private static String parseValue(Scan scan) {
        var value = new StringBuilder();
        scan.whileFor(Scan.condNotSpace(), value::append);
        return value.toString();
    }

    private static String parseName(Scan scan) {
        var name = new StringBuilder();
        while (scan.hasChar() && scan.isIdentifier())
            name.append(scan.currentAndNext());
        if (scan.notIsSpace() && scan.notIs('=')) {
            name.append(scan.current());
            throw new RuntimeException(
                    "Invalid character '%s' in parameter '%s'".formatted(scan.current(), name));
        }
        return name.toString();
    }
}
