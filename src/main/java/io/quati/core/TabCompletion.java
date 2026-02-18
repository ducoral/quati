package io.quati.core;

import java.util.Arrays;
import java.util.List;

public class TabCompletion {

    final Quati quati;

    public TabCompletion(Quati quati) {
        this.quati = quati;
    }

    public void execute(String[] args) {
        if (args.length == 0 || !args[0].equals("quati"))
            return;

        args = tail(args);
        if (args.length == 0)
            outputAndExit(quati.commandNames());

        var command = args[0];

        if (quati.contains(command))
            completionCommand(command, tail(args));

        if (quati.containsStartWith(command))
            outputAndExit(quati.commandNames());
    }

    private void completionCommand(String command, String[] args) {
        var info = quati.info(command);
        if (args.length == 0)
            outputAndExit(info.actionNames());

        var action = args[0];
        if (info.contains(action))
            completionAction(info, action, tail(args));

        if (info.containsStartWith(action))
            outputAndExit(info.actionNames());
    }

    private void completionAction(Command.Info info, String action, String[] args) {

        outputAndExit(List.of("um", "dois", "tres", "quatro"));
    }

    private void outputAndExit(List<String> completion) {
        completion.sort(String::compareTo);
        quati.output(completion
                .stream()
                .reduce("", (str, command) -> str + " " + command) + "%n");
        System.exit(0);
    }

    private static String[] tail(String[] args) {
        return args.length > 0
                ? Arrays.copyOfRange(args, 1, args.length)
                : new String[0];
    }
}