package io.quati.core;

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
        new Completion(this)
                .complete(args);
        new Execution(this)
                .execute(args);
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

    public FeatureInfo feature(String name) {
        return featureMap.get(name);
    }

    public void printUsage() {
        printUsage("FEATURE");
    }

    public void printUsage(String feature) {
        System.err.printf("Usage: quati %s COMMAND [options]%n", feature);
    }

    public void output(String format, Object... args) {
        System.out.printf(AnsiColor.filter(format), args);
    }

    public void error(String format, Object... args) {
        System.err.printf(AnsiColor.filter(format), args);
    }

    public void errorAndExit(String format, Object... args) {
        error(format, args);
        System.exit(0);
    }
}