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

    @Argument(label = "SOURCE DESTINATION", description = "data sources name of origin and destination", arity = TWO)
    List<String> sourceDestination;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        if (argPos == 1)
            Utils.completeArg(ctx.datasource().names(), value, null, candidates);
    }

    @Override
    public void execute(Context ctx) {
        var feature = ctx.datasource();
        var source = sourceDestination.getFirst();
        if (!feature.names().contains(source)) {
            feature.errorNotExists(source);
            return;
        }
        var destination = sourceDestination.getLast();
        var ds = feature.copy(source, destination);
        ctx.outputSuccessfully("Datasuource", source, "copied to `bb`%s`:`".formatted(destination));
        feature.print(ds, false);
    }
}