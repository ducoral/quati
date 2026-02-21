package io.quati.feature.driver;

import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Position;

import java.util.List;

public class DriverRemove implements Command {

    @Position(label = "DRIVER", desc = "name of driver to be removed")
    List<String> drivers;

    @Override
    public String name() {
        return "remove";
    }

    @Override
    public String desc() {
        return "remove JDBC driver";
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
