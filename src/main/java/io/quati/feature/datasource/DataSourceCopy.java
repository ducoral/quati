package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.util.List;

import static io.quati.api.Arity.TWO;

@Command(name = "copy", description = "copy a data source to a new one")
public class DataSourceCopy implements Action {

    @Argument(label = "SOURCE DESTINATION", desc = "data sources name of origin and destination", arity = TWO)
    List<String> sourceDestination;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        var names = ctx.quati().feature(DataSourceFeature.class).names();
        if (argPos == 1 && !names.contains(value))
            names.stream()
                    .map(Utils::candidate)
                    .forEach(candidates::add);
    }

    @Override
    public void execute(Context ctx) {
        var feature = ctx.quati().feature(DataSourceFeature.class);
        var source = sourceDestination.getFirst();
        if (!feature.names().contains(source)) {
            ctx.error("The DataSource `r`%s`:` do not exists!%n", source);
            return;
        }
        var destination = sourceDestination.getLast();
        var ds = feature.copy(source, destination);
        ctx.output("DataSource `b`%s`:` copied to `b`%s`:` `g`successfully!`:`%n", source, destination);
        feature.print(ds, false);
    }
}