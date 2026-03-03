package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.util.Utils;

@Command(name = "list", description = "list datasource")
public class DataSourceList implements Action {

    @Override
    public void execute(Context ctx) {
        ctx.output(
                "`b*`%s %s %s`:`%n",
                Utils.justifyLeft("datasource", 20),
                Utils.justifyLeft("driver", 15),
                Utils.justifyLeft("host", 30));
        for (var ds : ctx.datasource().dataSources())
            ctx.output(
                    "`*`%s`:` %s %s%n",
                    Utils.justifyLeft(ds.name(), 20),
                    Utils.justifyLeft(ds.driver(), 15),
                    Utils.justifyLeft(ds.host(), 30));
    }
}