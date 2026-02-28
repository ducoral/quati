package io.quati.core;

import io.quati.api.Context;
import io.quati.util.Utils;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record Completion(Quati quati) implements Completer {

    public void complete(String[] args) {
        if (args.length == 0 || !args[0].equals("quati"))
            return;
        var candidates = new ArrayList<Candidate>();
        completeFeature(Utils.tail(args), candidates);
        if (candidates.isEmpty())
            return;
        printAndExit(candidates.stream()
                .map(Candidate::value)
                .toList());
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        completeFeature(line.words().toArray(new String[0]), candidates);
    }

    public void completeFeature(String[] args, List<Candidate> candidates) {
        if (args.length == 0)
            candidates.addAll(quati.candidates());
        else if (quati.exists(args[0]))
            completeCommand(quati.featureInfo(args[0]), Utils.tail(args), candidates);
        else if (quati.existsStartingWith(args[0]))
            candidates.addAll(quati.candidates());
    }

    private void completeCommand(FeatureInfo feature, String[] args, List<Candidate> candidates) {
        if (args.length == 0)
            candidates.addAll(feature.candidates());
        else if (feature.exists(args[0]))
            completeParameter(
                    new QuatiContext(quati, feature),
                    feature.commandInfo(args[0]),
                    1,
                    Utils.tail(args),
                    candidates);
        else if (feature.existsStartingWith(args[0]))
            candidates.addAll(feature.candidates());
    }

    private void completeParameter(Context ctx, CommandInfo cmd, int pos, String[] args, List<Candidate> candidates) {
        if (args.length == 0) {
            if (cmd.hasPosition(pos))
                cmd.action().completeArg(ctx, pos, "", candidates);
        } else if (args[0].startsWith("-"))
            completeFlagOrOption(ctx, cmd, pos, args, candidates);
        else if (args.length == 1) {
            if (cmd.hasPosition(pos))
                cmd.action().completeArg(ctx, pos, args[0], candidates);
        } else {
            cmd.addArgument(args[0]);
            completeParameter(ctx, cmd, pos + 1, Utils.tail(args), candidates);
        }
    }

    private void completeFlagOrOption(Context ctx, CommandInfo cmd, int pos, String[] args, List<Candidate> candidates) {
        if (cmd.hasFlag(args[0])) {
            cmd.setFlag(args[0]);
            completeParameter(ctx, cmd, pos + 1, Utils.tail(args), candidates);
        } else if (cmd.hasOption(args[0])) {
            var opt = args[0];
            args = Utils.tail(args);
            if (args.length == 0)
                cmd.action().completeOpt(ctx, cmd.optionId(opt), "", candidates);
            else
                completeOption(ctx, cmd, opt, pos, args, candidates);
        } else if (cmd.existsStartingWith(args[0]))
            candidates.addAll(cmd.candidates());
    }

    private void completeOption(Context ctx, CommandInfo cmd, String opt, int pos, String[] args, List<Candidate> candidates) {
        if (args[0].startsWith("-"))
            completeParameter(ctx, cmd, pos, args, candidates);
        else if (args.length == 1) {
            if (cmd.hasRoomFor(opt))
                cmd.action().completeOpt(ctx, cmd.optionId(opt), args[0], candidates);
        } else {
            cmd.putOption(opt, args[0]);
            completeOption(ctx, cmd, opt, pos, Utils.tail(args), candidates);
        }
    }

    private void printAndExit(Collection<String> collection) {
        var list = new ArrayList<>(collection);
        list.sort(String::compareTo);
        System.out.println(String.join(" ", list.toArray(new String[0])));
        System.exit(0);
    }
}