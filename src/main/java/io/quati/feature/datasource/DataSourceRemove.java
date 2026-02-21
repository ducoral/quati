package io.quati.feature.datasource;

import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Position;

import java.util.List;

import static io.quati.api.Arity.ONE;

public class DataSourceRemove implements Command {

    @Position(label = "NAME", desc = "name of the datasource to be removed", arity = ONE)
    String datasource;

    @Override
    public String name() {
        return "remove";
    }

    @Override
    public String desc() {
        return "remove the datasource";
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
