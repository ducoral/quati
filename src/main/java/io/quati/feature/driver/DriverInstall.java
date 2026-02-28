package io.quati.feature.driver;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.util.ArrayList;
import java.util.List;

@Command(name = "install", description = "install JDBC driver")
public class DriverInstall implements Action {

    @Argument(label = "DRIVER", desc = "name of driver to be installed")
    List<String> drivers;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        var feature = ctx.quati().feature(DriverFeature.class);
        var available = new ArrayList<>(feature.available());
        available.removeAll(feature.installed());
        if (drivers != null)
            available.removeAll(drivers);
        available.remove(value);
        available.stream()
                .map(Utils::candidate)
                .forEach(candidates::add);
    }

    @Override
    public void execute(Context ctx) {
        if (drivers == null)
            return;
        drivers.forEach(ctx.quati().feature(DriverFeature.class)::install);
    }
}