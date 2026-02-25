package io.quati.feature.driver;

import io.quati.api.Action;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Flag;
import io.quati.util.Strs;

@Command(name = "list", desc = "list JDBC drivers")
public class DriverList implements Action {

    static final String INSTALLED = ":gg:[INSTALLED]::";
    static final String AVAILABLE = ":y:[AVAILABLE]::";

    @Flag(name = "-i|--installed", desc = "list only installed JDBC drivers")
    boolean installed;

    @Override
    public void execute(Context ctx) {
        var installedList = DriverFeature.getInstalled(ctx);
        for (var driver : DriverFeature.getAll())
            if (!installed || installedList.contains(driver)) {
                var status = installedList.contains(driver)
                        ? INSTALLED
                        : AVAILABLE;
                ctx.output("%s%s%n", Strs.justifyLeft(driver, 35), status);
            }
    }
}