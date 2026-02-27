package io.quati.feature.driver;


import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.util.ArrayList;
import java.util.List;

@Command(name = "load", description = "load JDBC driver to JVM")
public class DriverLoad implements Action {

    @Argument(label = "DRIVER", desc = "driver to be loaded")
    List<String> drivers;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        var feature = ctx.quati().feature(DriverFeature.class);
        var installed = new ArrayList<>(feature.getInstalled());
        if (drivers != null)
            installed.removeAll(drivers);
        installed.remove(value);
        installed.stream()
                .map(driver -> Utils.candidate(driver, null))
                .forEach(candidates::add);
    }

    @Override
    public void execute(Context ctx) {
        if (drivers == null)
            return;
        var feature = ctx.quati().feature(DriverFeature.class);
        for (var driver : drivers)
            feature.load(driver);

    }
}
