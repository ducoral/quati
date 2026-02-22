package io.quati.core;

import io.quati.util.Strs;

import java.util.ArrayList;
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
        completeFeature(Strs.tail(args));
    }

    private void completeFeature(String[] args) {
        if (args.length == 0)
            printAndExit(quati.features());
        else if (quati.exists(args[0]))
            completeCommand(quati.info(args[0]), Strs.tail(args));
        else if (quati.existsStartingWith(args[0]))
            printAndExit(quati.features());
    }

    private void completeCommand(FeatureInfo feature, String[] args) {
        if (args.length == 0)
            printAndExit(feature.commands());
        else if (feature.exists(args[0]))
            completeParameter(feature.info(args[0]), 1, Strs.tail(args));
        else if (feature.existsStartingWith(args[0]))
            printAndExit(feature.commands());
    }

    private void completeParameter(CommandInfo cmd, int pos, String[] args) {
        if (args.length == 0) {
            if (cmd.hasPosition(pos))
                printAndExit(completion -> cmd.action().completeArg(pos, "", completion));
        } else if (args[0].startsWith("-"))
            completeFlagOrOption(cmd, pos, args);
        else if (args.length == 1) {
            if (cmd.hasPosition(pos))
                printAndExit(completion -> cmd.action().completeArg(pos, args[0], completion));
        } else {
            cmd.addArgument(args[0]);
            completeParameter(cmd, pos + 1, Strs.tail(args));
        }
    }

    private void completeFlagOrOption(CommandInfo cmd, int pos, String[] args) {
        if (cmd.hasFlag(args[0])) {
            cmd.setFlag(args[0]);
            completeParameter(cmd, pos + 1, Strs.tail(args));
        } else if (cmd.hasOption(args[0])) {
            var opt = args[0];
            args = Strs.tail(args);
            if (args.length == 0)
                printAndExit(completion ->
                        cmd.action().completeOpt(cmd.optionId(opt), "", completion));
            else
                completeOption(cmd, opt, pos, args);
        } else if (cmd.existsStartingWith(args[0]))
            printAndExit(cmd.flagsAndOptions());
    }

    private void completeOption(CommandInfo cmd, String opt, int pos, String[] args) {
        if (args[0].startsWith("-"))
            completeParameter(cmd, pos, args);
        else if (args.length == 1) {
            if (cmd.hasRoomFor(opt))
                printAndExit(completion ->
                        cmd.action().completeOpt(cmd.optionId(opt), args[0], completion));
        } else {
            cmd.putOption(opt, args[0]);
            completeOption(cmd, opt, pos, Strs.tail(args));
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
}