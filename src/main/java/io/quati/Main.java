package io.quati;

import io.quati.core.Quati;
import io.quati.feature.datasource.DataSourceFeature;
import io.quati.feature.driver.DriverFeature;

import java.util.List;

public class Main {

    static final List<Class<?>> FEATURES = List.of(
            DriverFeature.class,
            DataSourceFeature.class);

    public static void main(String[] args) {
        new Quati(FEATURES)
                .execute(args);
    }
}
