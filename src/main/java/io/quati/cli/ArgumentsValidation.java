package io.quati.cli;

import java.util.Arrays;

public class ArgumentsValidation {

    final Quati quati;

    public ArgumentsValidation(Quati quati) {
        this.quati = quati;
    }

    public void execute(String[] args) {
        quati.output("validou: %s%n", Arrays.toString(args));
//        System.exit(0);
    }
}
