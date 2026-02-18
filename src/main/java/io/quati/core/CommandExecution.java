package io.quati.core;

public class CommandExecution {

    final Quati quati;

    public CommandExecution(Quati quati) {
        this.quati = quati;
    }

    public void execute(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(-1);
        }
        var command = args[0];
        if (quati.contains(command)) {
            System.exit(0);
        }
    }

    private void printAvailable() {

    }

    private void printUsage() {

    }
}
