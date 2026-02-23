package io.quati.core;

import io.quati.api.Context;
import io.quati.util.Strs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Quati {

    private final Map<String, FeatureInfo> featureMap = new HashMap<>();

    public Quati(List<Class<?>> featureClasses) {
        for (var featureClass : featureClasses) {
            var info = FeatureInfo.of(featureClass);
            featureMap.put(info.name(), info);
        }
    }

    public void execute(String[] args) {
        if (args.length > 0 && args[0].equals("quati")) {
            new Completion(this)
                    .complete(args);
            return;
        }
        if (args.length == 0)
            printUsage(null);
        var feature = info(args[0]);
        if (feature == null)
            error("Feature '%s' do not exists%n", args[0]);
        else
            executeFeature(feature, Strs.tail(args));
    }

    private void executeFeature(FeatureInfo feature, String[] args) {
        if (args.length > 0) {
            if (feature.exists(args[0])) {
                var command = feature.info(args[0]);
                new Validation(command)
                        .validate(Strs.tail(args));
                command
                        .action()
                        .execute(new QuatiContext(this, feature));
            } else
                error("Command '%s' do not exists from Feature '%s'%n", args[0], feature.name());
        } else
            printUsage(feature.name());
    }

    public Set<String> features() {
        return featureMap.keySet();
    }

    public boolean exists(String feature) {
        return featureMap.containsKey(feature);
    }

    public boolean existsStartingWith(String partial) {
        return featureMap
                .keySet()
                .stream()
                .anyMatch(feature -> feature.startsWith(partial));
    }

    public FeatureInfo info(String feature) {
        return featureMap.get(feature);
    }

    public void printUsage(String feature) {
        var argument = feature == null ? "FEATURE" : feature;
        System.err.printf("Usage: quati %s COMMAND [options]%n", argument);
    }

    public void output(String format, Object... args) {
        System.out.printf(AnsiColor.filter(format), args);
    }

    public void error(String format, Object... args) {
        System.err.printf(AnsiColor.filter(format), args);
    }
}