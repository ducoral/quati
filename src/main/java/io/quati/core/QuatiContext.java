package io.quati.core;

import io.quati.api.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QuatiContext implements Context {

    final Quati quati;

    final FeatureInfo feature;

    QuatiContext(Quati quati, FeatureInfo feature) {
        this.quati = quati;
        this.feature = feature;
    }

    @Override
    public void output(String format, Object... args) {
        quati.output(format, args);
    }

    @Override
    public void error(String format, Object... args) {
        quati.error(format, args);
    }

    @Override
    public Path repository() {
        var repo = quati
                .repository()
                .resolve(feature.name());
        try {
            Files.createDirectories(repo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return repo;
    }
}
