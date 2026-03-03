package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.util.List;

@Command(name = "remove", description = "remove the datasource")
public class DataSourceRemove implements Action {

    @Argument(label = "NAME", description = "datasource names to be removed")
    List<String> datasources;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        Utils.completeArg(ctx.datasource().names(), value, datasources, candidates);
    }

    @Override
    public void execute(Context ctx) {
        var feature = ctx.datasource();
        for (var datasource : datasources) {
            if (feature.names().contains(datasource)) {
                feature.remove(datasource);
                ctx.outputSuccessfully("Datasource", datasource, "removed");
            } else
                feature.errorNotExists(datasource);
        }
    }
}
