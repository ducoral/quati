package io.quati.core;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class TerminalContext {

    private static final AtomicReference<Terminal> TERMINAL_REF = new AtomicReference<>();

    public static void buildDefault() {
        try {
            TERMINAL_REF.set(TerminalBuilder.builder()
                    .system(true)
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void set(Terminal terminal) {
        TERMINAL_REF.set(terminal);
    }

    public static Terminal get() {
        return TERMINAL_REF.get();
    }
}
