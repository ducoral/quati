package io.quati.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class Completion {

    private final Quati quati;

    public Completion(Quati quati) {
        this.quati = quati;
    }

    public void complete(String[] args) {
        if (args.length == 0 || !args[0].equals("quati"))
            return;
        completeFeature(tail(args));
    }

    private void completeFeature(String[] args) {
        if (args.length == 0)
            printAndExit(quati.features());
        else if (quati.exists(args[0]))
            completeCommand(quati.info(args[0]), tail(args));
        else if (quati.existsStartingWith(args[0]))
            printAndExit(quati.features());
    }

    private void completeCommand(FeatureInfo feature, String[] args) {
        if (args.length == 0)
            printAndExit(feature.commands());
        else if (feature.exists(args[0]))
            completeParameter(feature.info(args[0]), 1, tail(args));
        else if (feature.existsStartingWith(args[0]))
            printAndExit(feature.commands());
    }

    private void completeParameter(CommandInfo command, int pos, String[] args) {
        if (args.length == 0) {
            if (command.hasPosition(pos))
                printAndExit(completion -> command.action().completeArg(pos, "", completion));
        } else if (args[0].startsWith("-"))
            completeFlagOrOption(command, pos, args);
        else if (args.length == 1) {
            if (command.hasPosition(pos))
                printAndExit(completion -> command.action().completeArg(pos, args[0], completion));
        } else {
            command.addArgument(args[0]);
            completeParameter(command, pos + 1, tail(args));
        }
    }

    private void completeFlagOrOption(CommandInfo command, int pos, String[] args) {
        if (command.hasFlag(args[0])) {
            command.setFlag(args[0]);
            completeParameter(command, pos + 1, tail(args));
        } else if (command.hasOption(args[0])) {
            var opt = args[0];
            args = tail(args);
            if (args.length == 0)
                printAndExit(completion -> command.action().completeOpt(opt, "", completion));
            else
                completeOption(command, opt, pos, args);
        } else if (command.existsStartingWith(args[0]))
            printAndExit(command.flagsAndOptions());
    }

    private void completeOption(CommandInfo cmd, String opt, int pos, String[] args) {
        if (args[0].startsWith("-"))
            completeParameter(cmd, pos, args);
        else if (args.length == 1) {
            if (cmd.hasRoomFor(opt))
                printAndExit(completion -> cmd.action().completeOpt(opt, args[0], completion));
        } else {
            cmd.putOption(opt, args[0]);
            completeOption(cmd, opt, pos, tail(args));
        }
    }

    private void printAndExit(Consumer<List<String>> completion) {
        var list = new ArrayList<String>();
        completion.accept(list);
        printAndExit(list);
    }

    private void printAndExit(Collection<String> collection) {
        var list = new ArrayList<>(collection);
        list.sort(String::compareTo);
        System.out.println(String.join(" ", list.toArray(new String[0])));
        System.exit(0);
    }

    private String[] tail(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}