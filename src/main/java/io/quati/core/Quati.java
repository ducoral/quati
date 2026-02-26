package io.quati.core;

import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Quati {

    private static final String APP_NAME = "QuatiCLI";

    private final Map<String, FeatureInfo> featuresMap = new HashMap<>();

    private final Path home;

    public Quati(List<Class<?>> featureClasses) {
        for (var featureClass : featureClasses) {
            var info = FeatureInfo.of(featureClass);
            featuresMap.put(info.name(), info);
        }
        home = Path.of(System.getenv("HOME"), ".quati");
    }

    public void execute(String[] args) {
        new ReadEvalPrintLoop(this)
                .doREPL(args);
        new Completion(this)
                .complete(args);
        new Execution(this)
                .execute(args);
    }

    public Path repository() {
        return home.resolve("repo");
    }

    public Set<String> features() {
        return featuresMap.keySet();
    }

    public List<Candidate> candidates() {
        return featuresMap.values().stream()
                .map(feature -> Utils.candidate(feature.name(), feature.desc()))
                .toList();
    }

    public boolean exists(String feature) {
        return featuresMap.containsKey(feature);
    }

    public boolean existsStartingWith(String partial) {
        return featuresMap
                .keySet()
                .stream()
                .anyMatch(feature -> feature.startsWith(partial));
    }

    public FeatureInfo feature(String name) {
        return featuresMap.get(name);
    }

    public void printUsage() {
        printUsage("FEATURE");
    }

    public void printUsage(String feature) {
        System.err.printf("Usage: quati %s COMMAND [options]%n", feature);
    }

    public void output(String format, Object... args) {
        System.out.print(filter(format, args));
    }

    public void error(String format, Object... args) {
        System.err.print(filter(format, args));
    }

    public void printNameAndVersion() {
        String version = "unknown";
        try (var input = Quati.class
                .getClassLoader()
                .getResourceAsStream("META-INF/maven/io.quati/quati/pom.properties")) {
            var props = new Properties();
            props.load(input);
            version = props.getProperty("version", version);
        } catch (IOException e) {
            //
        }
        output("%s v%s%n", APP_NAME, version);
    }

    private String filter(String format, Object... args) {
        return AnsiColor.filter(format.formatted(args));
    }
}