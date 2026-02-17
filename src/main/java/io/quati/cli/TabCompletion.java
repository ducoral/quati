package io.quati.cli;

public class TabCompletion {

    final Quati quati;

    public TabCompletion(Quati quati) {
        this.quati = quati;
    }

    public void execute(String[] args) {
        if (args.length == 0 || !args[0].equals("quati"))
            return;

        var infoMap = quati.infoMap();
        System.exit(0);
    }
}
