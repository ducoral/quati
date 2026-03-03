package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Flag;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.util.List;

@Command(name = "info", description = "display the datasource configuration")
public class DataSourceInfo implements Action {

    @Argument(label = "NAME", description = "datasource names to be displayed")
    List<String> datasources;

    @Flag(name = "-S|--show-password", description = "show the password")
    boolean showPassword;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        Utils.completeArg(ctx.datasource().names(), value, datasources, candidates);
    }

    @Override
    public void execute(Context ctx) {
        var feature = ctx.datasource();
        for (var datasource : datasources) {
            var ds = feature.find(datasource);
            if (ds != null) {
                ctx.output("`b`%s`:`%n", "-".repeat(30));
                feature.print(ds, showPassword);
            } else
                feature.errorNotExists(datasource);
        }
    }
}