package io.quati.core;

import io.quati.api.FeatureInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static io.quati.api.FeatureInfo.*;

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
            printAndExit(quati.featureNames());
        else if (quati.hasFeature(args[0]))
            completeCommand(quati.feature(args[0]), tail(args));
        else if (quati.hasFeatureStartsWith(args[0]))
            printAndExit(quati.featureNames());
    }

    private void completeCommand(FeatureInfo feature, String[] args) {
        if (args.length == 0)
            printAndExit(feature.cmds().keySet());
        else if (feature.hasCmd(args[0]))
            completeParameter(feature.cmds().get(args[0]), 1, tail(args));
        else if (feature.hasCmdStartsWith(args[0]))
            printAndExit(feature.cmds().keySet());
    }

    private void completeParameter(CmdInfo cmd, int pos, String[] args) {
        if (args.length == 0) {
            if (cmd.hasPos(pos))
                printAndExit(completion -> cmd.obj().tabComp(pos, "", completion));
        } else if (args[0].startsWith("-"))
            completeFlagOrOption(cmd, pos, args);
        else if (args.length == 1) {
            if (cmd.hasPos(pos))
                printAndExit(completion -> cmd.obj().tabComp(pos, args[0], completion));
        } else {
            cmd.addPosValue(args[0]);
            completeParameter(cmd, pos + 1, tail(args));
        }
    }

    private void completeFlagOrOption(CmdInfo cmd, int pos, String[] args) {
        if (cmd.hasFlag(args[0])) {
            cmd.setFlag(args[0]);
            completeParameter(cmd, pos + 1, tail(args));
        } else if (cmd.hasOpt(args[0])) {
            var opt = args[0];
            args = tail(args);
            if (args.length == 0)
                printAndExit(completion -> cmd.obj().tabComp(opt, "", completion));
            else
                completeOption(cmd, opt, pos, args);
        } else if (cmd.hasOptFlagStartsWith(args[0]))
            printAndExit(cmd.optFlagNames());
    }

    private void completeOption(CmdInfo cmd, String opt, int pos, String[] args) {
        if (args[0].startsWith("-"))
            completeParameter(cmd, pos, args);
        else if (args.length == 1) {
            if (cmd.hasRoomFor(opt))
                printAndExit(completion -> cmd.obj().tabComp(opt, args[0], completion));
        } else {
            cmd.putOpt(opt, args[0]);
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