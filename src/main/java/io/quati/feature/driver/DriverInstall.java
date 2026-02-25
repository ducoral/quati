package io.quati.feature.driver;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;

import java.util.ArrayList;
import java.util.List;

@Command(name = "install", desc = "install JDBC driver")
public class DriverInstall implements Action {

    @Argument(label = "DRIVER", desc = "name of driver to be installed")
    List<String> drivers;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<String> completion) {
        var available = new ArrayList<>(DriverFeature.getAll());
        available.removeAll(DriverFeature.getInstalled(ctx));
        if (drivers != null)
            available.removeAll(drivers);
        available.remove(value);
        completion.addAll(available);
    }

    @Override
    public void execute(Context ctx) {
        if (drivers == null)
            return;
        drivers.forEach(driver -> DriverFeature.install(ctx, driver));
    }
}