package io.quati.feature.datasource;


import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;

import static io.quati.api.Arity.ONE;

@Command(name = "info", desc = "displays the datasource configuration")
public class DataSourceInfo implements Action {

    @Argument(label = "NAME", desc = "name of the datasource to be displayed", arity = ONE)
    String datasource;

    @Override
    public void execute(Context ctx) {

    }
}