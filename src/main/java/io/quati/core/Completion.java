package io.quati.core;

import io.quati.api.FeatureInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
            printCompletion(quati.featureNames());
        if (quati.hasFeature(args[0]))
            completeCommand(quati.feature(args[0]), tail(args));
        if (quati.hasFeatureStartsWith(args[0]))
            printCompletion(quati.featureNames());
    }

    private void completeCommand(FeatureInfo feature, String[] args) {
        if (args.length == 0)
            printCompletion(feature.cmds().keySet());
        if (feature.hasCmd(args[0]))
            completeArguments(feature.cmds().get(args[0]), tail(args));
        if (feature.hasCmdStartsWith(args[0]))
            printCompletion(feature.cmds().keySet());
    }

    private void completeArguments(FeatureInfo.CmdInfo cmd, String[] args) {
        if (args.length == 0)
            printCompletion(List.of("laranja", "goiaba", "abacaxi", "limão"));
        if (cmd.hasOptOrFlag(args[0]))
            printCompletion(List.of("mamão", "melancia", "caju", "mexerica"));
        if (cmd.hasOptOrFlagStartsWith(args[0]))
            printCompletion(cmd.optAndFlagNames());
    }

    private void printCompletion(Collection<String> collection) {
        var list = new ArrayList<>(collection);
        list.sort(String::compareTo);
        var array = list.toArray(new String[0]);
        System.out.println(String.join(" ", array));
        System.exit(0);
    }

    private String[] tail(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }
}