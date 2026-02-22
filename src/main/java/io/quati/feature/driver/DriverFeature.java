package io.quati.feature.driver;

import io.quati.api.Feature;

@Feature(
        name = "driver",
        desc = "JDBC driver manager",
        commands = {
                DriverList.class,
                DriverInstall.class,
                DriverRemove.class
        })
public class DriverFeature {
}