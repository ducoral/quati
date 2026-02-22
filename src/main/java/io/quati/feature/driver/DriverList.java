package io.quati.feature.driver;

import io.quati.api.Action;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Flag;

@Command(name = "list", desc = "list JDBC drivers")
public class DriverList implements Action {

    @Flag(name = "-i|--installed", desc = "list only installed JDBC drivers")
    boolean installed;

    @Override
    public void execute(Context ctx) {

    }
}