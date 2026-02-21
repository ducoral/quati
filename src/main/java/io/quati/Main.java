package io.quati;

import io.quati.api.Feature;
import io.quati.core.Quati;
import io.quati.feature.datasource.DataSourceFeature;
import io.quati.feature.driver.DriverFeature;

import java.lang.reflect.Field;
import java.util.List;

public class Main {

    static final List<Class<? extends Feature>> FEATURES = List.of(
            DriverFeature.class,
            DataSourceFeature.class
    );

    public static void main(String[] args) {

        new Quati(FEATURES)
                .execute(args);
    }
}
