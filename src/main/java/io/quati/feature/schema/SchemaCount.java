package io.quati.feature.schema;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Arity;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Option;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Command(name = "count", description = "count schema items")
public class SchemaCount implements Action {

    @Argument(label = "SCHEMA", description = "scheme that will be accounted for", arity = Arity.ONE)
    private String schemaName;

    @Option(name = "-t|--table", label = "TABLE", description = "table for counting records")
    private String tableName;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        Utils.completeCandidates(ctx.schema().names(), value, null, candidates, true);
    }

    @Override
    public void completeOpt(Context ctx, String opt, String value, List<Candidate> candidates) {
        var schema = ctx.schema().find(schemaName);
        if (schemaName == null)
            return;
        Utils.completeCandidates(new ArrayList<>(schema.tables().keySet()), value, null, candidates, true);
    }

    @Override
    public void execute(Context ctx) {
        var schema = ctx.schema().find(schemaName);
        if (schema == null)
            ctx.errorNotExists("schema", schemaName);
        else if (tableName == null || tableName.isBlank())
            ctx.output("%d tables%n", schema.tables().size());
        else {
            ctx.startTarget(schemaName);
            try (var conn = ctx.datasource().connect(schemaName)) {
                var datasource = ctx.datasource().find(schemaName);
                var select = "SELECT COUNT(*) FROM %s.%s".formatted(datasource.schema(), tableName);
                try (var rs = conn.createStatement().executeQuery(select)) {
                    if (rs.next())
                        ctx.output("%d records%n", rs.getInt(1));
                    else
                        ctx.error("no result set");
                }
                ctx.endTargetSuccessfully("datasource", schemaName, "queried");
            } catch (SQLException e) {
                ctx.error(e);
            }
        }
    }
}