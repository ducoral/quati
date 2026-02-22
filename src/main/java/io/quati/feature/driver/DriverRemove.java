package io.quati.feature.driver;

import io.quati.api.Action;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Argument;

import java.util.List;

@Command(name = "remove", desc = "remove JDBC driver")
public class DriverRemove implements Action {

    @Argument(label = "DRIVER", desc = "name of driver to be removed")
    List<String> drivers;

    @Override
    public void completeArg(int argPos, String value, List<String> completion) {
    }

    @Override
    public void completeOpt(String opt, String value, List<String> completion) {
    }

    @Override
    public void execute(Context ctx) {

    }
}
