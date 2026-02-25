package io.quati.feature.driver;

import io.quati.api.Action;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Argument;

import java.util.ArrayList;
import java.util.List;

@Command(name = "remove", desc = "remove JDBC driver")
public class DriverRemove implements Action {

    @Argument(label = "DRIVER", desc = "name of driver to be removed")
    List<String> drivers;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<String> completion) {
        var installed = new ArrayList<>(DriverFeature.getInstalled(ctx));
        if (drivers != null)
            installed.removeAll(drivers);
        installed.remove(value);
        completion.addAll(installed);
    }

    @Override
    public void execute(Context ctx) {
        if (drivers == null)
            return;
        for (var driver : drivers)
            DriverFeature.remove(ctx, driver);
    }
}
