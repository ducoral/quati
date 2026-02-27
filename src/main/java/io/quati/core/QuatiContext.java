package io.quati.core;

import io.quati.api.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

record QuatiContext(Quati quati, FeatureInfo featureInfo) implements Context {

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
                .resolve(featureInfo.name());
        try {
            Files.createDirectories(repo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return repo;
    }
}
