package io.quati.feature.driver;

import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Flag;

import java.util.List;

public class DriverList implements Command {

    @Flag(names = "-i|--installed", desc = "list only installed JDBC drivers")
    boolean installed;

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String desc() {
        return "list JDBC drivers";
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
