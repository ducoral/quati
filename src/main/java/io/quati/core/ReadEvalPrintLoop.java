package io.quati.core;

import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.TerminalBuilder;

public record ReadEvalPrintLoop(Quati quati) {

    public void doREPL(String[] args) {
        if (args.length > 0)
            return;

        try (var terminal = TerminalBuilder.builder()
                .system(true)
                .build()) {
            var reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(new Completion(quati))
                    .build();
            quati.printNameAndVersion();
            while (true) {
                var line = reader.readLine(AnsiColor.filter("`x784a20*`quati>`:` "));
                if (line.isBlank())
                    continue;
                if (isExist(line))
                    break;
                args = line.split("\\s+");
                quati.execute(args);
            }
            System.exit(0);
        } catch (Exception e) {
            quati.error(e.getMessage());
        }
    }

    private static boolean isExist(String input) {
        var lower = input.toLowerCase();
        return "exit".equals(lower)
                || "quit".equals(lower)
                || "q".equals(lower);
    }
}