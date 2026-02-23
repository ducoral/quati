package io.quati.core;

import io.quati.api.Context;

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
}
