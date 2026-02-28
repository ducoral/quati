package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.sql.SQLException;
import java.util.List;

import static io.quati.api.Arity.ONE;

@Command(name = "test", description = "test datasource connection")
public class DataSourceTest implements Action {

    @Argument(label = "NAME", desc = "name of the datasource to be tested", arity = ONE)
    String datasource;

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
        if (feature.names().contains(datasource))
            try (var conn = feature.connect(datasource)) {
                ctx.output("The DataSource `bb`%s`:` connected `bb`successfully!`:`%n", datasource);
            } catch (SQLException e) {
                ctx.error("`r`Error: %s`:`%n", e.getMessage());
            }
        else
            ctx.error("The DataSource `r`%s`:` do not exists!%n", datasource);
    }
}
