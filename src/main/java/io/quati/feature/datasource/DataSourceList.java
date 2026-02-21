package io.quati.feature.datasource;

import io.quati.api.Command;
import io.quati.api.Context;

import java.util.List;

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
    public void tabComp(int pos, String value, List<String> compList) {

    }

    @Override
    public void tabComp(String opt, String value, List<String> compList) {

    }

    @Override
    public void exec(Context ctx) {

    }
}
