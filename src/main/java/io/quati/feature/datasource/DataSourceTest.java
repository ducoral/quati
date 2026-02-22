package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;

import static io.quati.api.Arity.ONE;

@Command(name = "test", desc = "test datasource connection")
public class DataSourceTest implements Action {

    @Argument(label = "NAME", desc = "name of the datasource to be tested", arity = ONE)
    String datasource;

    @Override
    public void execute(Context ctx) {
    }
}
