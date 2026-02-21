package io.quati.feature.driver;

import io.quati.api.Position;
import io.quati.api.Command;
import io.quati.api.Context;

import java.util.List;

public class DriverInstall implements Command {

    @Position(label = "DRIVER", desc = "name of driver to be installed")
    List<String> drivers;

    @Override
    public String name() {
        return "install";
    }

    @Override
    public String desc() {
        return "install JDBC driver";
    }

    @Override
    public void tabComp(int pos, String value, List<String> compList) {

    }

    @Override
    public void tabComp(String opt, String value, List<String> compList) {

    }

    @Override
    public void exec(Context ctx) {

    }
}