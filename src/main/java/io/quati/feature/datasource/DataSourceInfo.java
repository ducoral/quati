package io.quati.feature.datasource;


import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Flag;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.util.List;

import static io.quati.api.Arity.ONE;

@Command(name = "info", description = "displays the datasource configuration")
public class DataSourceInfo implements Action {

    @Argument(label = "NAME", desc = "name of the datasource to be displayed", arity = ONE)
    String datasource;

    @Flag(name = "-p", description = "show the password")
    boolean showPassword;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        if (datasource == null)
            ctx.quati()
                    .feature(DataSourceFeature.class)
                    .names()
                    .forEach(name -> candidates.add(Utils.candidate(name)));
    }

    @Override
    public void execute(Context ctx) {
        var feature = ctx.quati().feature(DataSourceFeature.class);
        var ds = feature.find(datasource);
        if (ds == null) {
            ctx.error("The DataSource `r`%s`:` do not exists!%n", datasource);
            return;
        }
        ctx.output("`b`name     :`:` %s%n", ds.name());
        ctx.output("`b`driver   :`:` %s%n", ds.driver());
        ctx.output("`b`host     :`:` %s%n", ds.host());
        ctx.output("`b`port     :`:` %s%n", ds.port());
        ctx.output("`b`database :`:` %s%n", ds.database());
        ctx.output("`b`user     :`:` %s%n", ds.user());
        ctx.output("`b`password :`:` %s%n", showPassword ? ds.password() : "*".repeat(ds.password().length()));
    }
}