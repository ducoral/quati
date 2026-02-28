package io.quati.feature.driver;

import io.quati.api.Action;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Flag;
import io.quati.util.Utils;

@Command(name = "list", description = "list JDBC drivers")
public class DriverList implements Action {

    static final String INSTALLED = "`gg*`[INSTALLED]`:`";
    static final String AVAILABLE = "`yy`[AVAILABLE]`:`";

    @Flag(name = "-i|--installed", description = "list only installed JDBC drivers")
    boolean installed;

    @Override
    public void execute(Context ctx) {
        var feature = ctx.quati().feature(DriverFeature.class);
        var installedList = feature.installed();
        for (var driver : feature.available())
            if (!installed || installedList.contains(driver)) {
                var status = installedList.contains(driver)
                        ? INSTALLED
                        : AVAILABLE;
                ctx.output("%s%s%n", Utils.justifyLeft(driver, 25), status);
            }
    }
}