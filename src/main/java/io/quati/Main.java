package io.quati;

import picocli.CommandLine;

public class Main {

    public static void main(String... args) {
        int exitCode = new CommandLine(new CheckSum()).execute(args);
        System.exit(exitCode);
    }
}
