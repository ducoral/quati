package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.sql.SQLException;
import java.util.List;

@Command(name = "test", description = "test datasource connection")
public class DataSourceTest implements Action {

    @Argument(label = "NAME", description = "datasource names to be tested")
    List<String> datasources;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        Utils.completeCandidates(ctx.datasource().names(), value, datasources, candidates, false);
    }

    @Override
    public void execute(Context ctx) {
        var feature = ctx.datasource();
        for (var datasource : datasources) {
            ctx.startTarget(datasource);
            if (feature.names().contains(datasource))
                try (var conn = feature.connect(datasource)) {
                    ctx.endTargetSuccessfully("datasource", datasource, "connected");
                } catch (SQLException e) {
                    ctx.error(new RuntimeException("datasource %s connection test failed".formatted(datasource), e));
                }
            else
                ctx.errorNotExists("datasource", datasource);
        }
    }
}
