package io.quati.core;

import io.quati.util.Scan;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.TerminalBuilder;

import java.util.ArrayList;

public record ReadEvalPrintLoop(Quati quati) {

    public void readLine(String[] args) {
        if (args.length > 0)
            return;

        try (var terminal = TerminalBuilder.builder()
                .system(true)
                .build()) {
            TerminalContext.set(terminal);
            var reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new Completion(quati))
                    .history(new DefaultHistory())
                    .variable(LineReader.HISTORY_FILE, quati.home().resolve("history.txt"))
                    .variable(LineReader.HISTORY_SIZE, 1000)
                    .variable(LineReader.HISTORY_FILE_SIZE, 2000)
                    .build();
            reader.setOpt(LineReader.Option.HISTORY_IGNORE_SPACE);
            reader.setOpt(LineReader.Option.HISTORY_BEEP);
            quati.printNameAndVersion();
            while (true)
                try {
                    var line = reader.readLine(AnsiColor.filter("`x784a20*`quati>`:` "));
                    if (line.isBlank())
                        continue;
                    if (isExist(line))
                        break;
                    new Execution(quati)
                            .execute(parseLine(line));
                } catch (Exception e) {
                    quati.error(e);
                }
            System.exit(0);
        } catch (Exception e) {
            quati.error(e);
        }
    }

    private static String[] parseLine(String line) {
        var parsed = new ArrayList<String>();
        var lexeme = new StringBuilder();
        var scan = new Scan(line);
        scan.skipSpaces();
        while (!scan.isEof()) {
            if (scan.isSpace()) {
                if (!lexeme.isEmpty()) {
                    parsed.add(lexeme.toString());
                    lexeme.setLength(0);
                }
                scan.skipSpaces();
            } else if (scan.is('"'))
                scan
                        .accept('"')
                        .whileFor(Scan.condNotEq('"'), lexeme::append)
                        .accept('"');
            else
                lexeme.append(scan.currentAndNext());
        }
        if (!lexeme.isEmpty())
            parsed.add(lexeme.toString());
        return parsed.toArray(new String[0]);
    }

    private static boolean isExist(String line) {
        var lower = line.toLowerCase();
        return "exit".equals(lower)
                || "quit".equals(lower)
                || "q".equals(lower);
    }
}