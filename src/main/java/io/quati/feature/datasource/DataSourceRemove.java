package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;

import static io.quati.api.Arity.ONE;

@Command(name = "remove", description = "remove the datasource")
public class DataSourceRemove implements Action {

    @Argument(label = "NAME", desc = "name of the datasource to be removed", arity = ONE)
    String datasource;

    @Override
    public void execute(Context ctx) {

    }
}
