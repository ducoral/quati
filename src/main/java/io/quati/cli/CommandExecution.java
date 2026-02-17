package io.quati.cli;

import java.util.Arrays;

public class CommandExecution {

    final Quati quati;

    public CommandExecution(Quati quati) {
        this.quati = quati;
    }

    public void execute(String[] args) {
        quati.output("executou: %s%n", Arrays.toString(args));
        System.exit(0);
    }
}
