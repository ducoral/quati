package io.quati.feature.datasource;

import io.quati.api.Command;
import io.quati.api.Context;

public class DataSourceList implements Command {

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String desc() {
        return "list datasource";
    }

    @Override
    public void exec(Context ctx) {

    }
}
