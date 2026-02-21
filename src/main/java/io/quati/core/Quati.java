package io.quati.core;

import io.quati.api.Feature;
import io.quati.api.FeatureInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Quati {

    private final Map<String, FeatureInfo> features = new HashMap<>();

    public Quati(List<Class<? extends Feature>> featuresList) {
        try {
            for (var feature : featuresList) {
                var instance = feature.getDeclaredConstructor().newInstance();
                features.put(instance.name(), instance.info());
            }
        } catch (Exception e) {
            System.out.println(AnsiColor.RED.fg(e.getMessage()));
        }
    }

    public void execute(String[] args) {
        new Completion(this).complete(args);
    }

    public Set<String> featureNames() {
        return features.keySet();
    }

    public boolean hasFeature(String feature) {
        return features.containsKey(feature);
    }

    public boolean hasFeatureStartsWith(String partial) {
        return features
                .keySet()
                .stream()
                .anyMatch(feature -> feature.startsWith(partial));
    }

    public FeatureInfo feature(String name) {
        return features.get(name);
    }
}