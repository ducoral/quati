package io.quati.feature.schema;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.util.List;

@Command(name = "update", description = "update the schema from datasource")
public class SchemaUpdate implements Action {

    @Argument(label = "DATASOURCE", description = "datasource name to update the schema")
    private List<String> datasources;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        Utils.completeCandidates(ctx.datasource().names(), value, datasources, candidates, false);
    }

    @Override
    public void execute(Context ctx) {
        var feature = ctx.schema();
        var names = ctx.datasource().names();
        for (var datasource : datasources)
            if (names.contains(datasource)) {
                feature.update(datasource);
            } else
                ctx.errorNotExists("datasource", datasource);
    }
}
