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

import static io.quati.feature.schema.SchemaFeature.*;

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
            countTables(schema, ctx);
        else
            countRecords(schema, ctx);
    }

    private void countTables(Schema schema, Context ctx) {
        ctx.startTarget(schemaName);
        int maxCol = 0, maxExp = 0, maxImp = 0;
        for (var table : schema.tables().values()) {
            maxCol = Math.max(maxCol, table.columns().size());
            maxExp = Math.max(maxExp, table.exported().size());
            maxImp = Math.max(maxImp, table.imported().size());
        }
        List<String> colList = new ArrayList<>(), expList = new ArrayList<>(), impList = new ArrayList<>();
        for (var name : schema.tables().keySet()) {
            var table = schema.tables().get(name);
            if (table.columns().size() == maxCol)
                colList.add(name);
            if (table.exported().size() == maxExp)
                expList.add(name);
            if (table.imported().size() == maxImp)
                impList.add(name);
        }
        ctx.output("`*`table count  :`:` %d%n", schema.tables().size());
        ctx.output("`*`max `y`columns`:z`  :`:` %d [%s]%n", maxCol, String.join(", ", colList));
        ctx.output("`*`max `c`exported`:z` :`:` %d [%s]%n", maxExp, String.join(", ", expList));
        ctx.output("`*`max `b`imported`:z` :`:` %d [%s]%n", maxImp, String.join(", ", impList));
        ctx.endTargetSuccessfully("schema", schemaName, "counted");
    }

    private void countRecords(Schema schema, Context ctx) {
        ctx.startTarget(schemaName);
        try (var conn = ctx.datasource().connect(schemaName)) {
            var datasource = ctx.datasource().find(schemaName);
            var select = "SELECT COUNT(*) FROM %s.%s".formatted(datasource.schema(), tableName);
            try (var rs = conn.createStatement().executeQuery(select)) {
                if (rs.next())
                    ctx.output("`*`record count   :`:` %d%n", rs.getInt(1));
            }
            var table = schema.tables().get(tableName);
            ctx.output("`*y`column`:z` count   :`:` %d%n", table.columns().size());
            ctx.output("`*c`exported`:z` count :`:` %d%n", table.exported().size());
            ctx.output("`*b`imported`:z` count :`:` %d%n", table.imported().size());
            ctx.endTargetSuccessfully("datasource", schemaName, "queried");
        } catch (SQLException e) {
            ctx.error(e);
        }
    }
}