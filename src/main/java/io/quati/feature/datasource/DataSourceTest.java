package io.quati.feature.datasource;

import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Position;

import static io.quati.api.Arity.ONE;

public class DataSourceTest implements Command {

    @Position(label = "NAME", desc = "name of the datasource to be tested", arity = ONE)
    String datasource;

    @Override
    public String name() {
        return "test";
    }

    @Override
    public String desc() {
        return "test datasource connection";
    }

    @Override
    public void exec(Context ctx) {

    }
}
