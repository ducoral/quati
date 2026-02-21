package io.quati.feature.driver;

import io.quati.api.Command;
import io.quati.api.Feature;

import java.util.List;

public class DriverFeature implements Feature {

    @Override
    public String name() {
        return "driver";
    }

    @Override
    public String desc() {
        return "JDBC driver manager";
    }

    @Override
    public List<Class<? extends Command>> cmds() {
        return List.of(
                DriverList.class,
                DriverInstall.class,
                DriverRemove.class
        );
    }
}
