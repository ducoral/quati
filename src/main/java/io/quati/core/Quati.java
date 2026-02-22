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
        new Completion(this).complete(args);
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
}