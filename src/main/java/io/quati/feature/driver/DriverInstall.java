package io.quati.feature.driver;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;

import java.util.List;

@Command(name = "install", desc = "install JDBC driver")
public class DriverInstall implements Action {

    @Argument(label = "DRIVER", desc = "name of driver to be installed")
    List<String> drivers;

    @Override
    public void execute(Context ctx) {

    }
}