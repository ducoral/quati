package io.quati.feature.schema;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Arity;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Option;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static io.quati.util.Utils.leftJust;
import static io.quati.util.Utils.line;

@Command(name = "data", description = "query database data")
public class SchemaData implements Action {

    @Argument(label = "SCHEMA", description = "scheme that data will be queried", arity = Arity.ONE)
    private String name;

    @Option(name = "-t|--table", label = "TABLE", description = "table to query data", arity = Arity.ONE)
    private String table;

    @Option(name = "-c|--column", label = "COLUMN", description = "columns for SELECT clause", arity = Arity.ZERO_OR_MORE)
    private List<String> columns;

    @Option(name = "-w|--where", label = "CONDITION", description = "WHERE clause", arity = Arity.ZERO_OR_MORE)
    private List<String> where;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        Utils.completeCandidates(ctx.schema().names(), value, null, candidates, true);
    }

    @Override
    public void completeOpt(Context ctx, String opt, String value, List<Candidate> candidates) {
        var schema = ctx.schema().find(name);
        if (schema == null)
            return;
        if (opt.equals("-t") && value != null && !value.isBlank())
            Utils.completeCandidates(new ArrayList<>(schema.tables().keySet()), value, null, candidates, true);
        else if ((opt.equals("-c") || opt.equals("-w")) && schema.tables().containsKey(table)) {
            var arguments = opt.equals("-c")
                    ? columns
                    : where;
            var reference = schema
                    .tables()
                    .get(table)
                    .columns()
                    .stream()
                    .map(SchemaFeature.Column::name)
                    .toList();
            Utils.completeCandidates(reference, value, arguments, candidates, false);
        }
    }

    @Override
    public void execute(Context ctx) {
        var schema = ctx.schema().find(name);
        if (schema == null) {
            ctx.errorNotExists("schema", name);
            return;
        }
        var vendor = ctx.datasource().vendor(name);
        if (vendor == null) {
            ctx.errorNotExists("driver", name);
            return;
        }
        ctx.startTarget(name);
        try (var conn = ctx.datasource().connect(name)) {
            var limit = ctx.height() - 5;
            var datasource = ctx.datasource().find(name);
            var fields = columns == null || columns.isEmpty()
                    ? "*"
                    : String.join(",", columns);
            var condition = where == null || where.isEmpty()
                    ? ""
                    : String.join(" ", where);
            var select = vendor.selectTable(datasource.schema(), table, fields, condition, limit);
            ctx.output("select: %s%n", select);
            try (var rs = conn.createStatement().executeQuery(select)) {
                printResultSet(rs, limit, ctx);
                ctx.endTargetSuccessfully("schema", name, "queried");
            }
        } catch (SQLException e) {
            ctx.error(e);
        }
    }

    private void printResultSet(ResultSet rs, int limit, Context ctx) throws SQLException {
        var md = rs.getMetaData();
        var columns = new ArrayList<String>();
        var sizes = new ArrayList<Integer>();
        for (var column = 1; column <= md.getColumnCount(); column++) {
            columns.add(md.getColumnName(column));
            sizes.add(columns.getLast().length());
        }
        var records = new ArrayList<List<String>>();
        int count = 0;
        while (rs.next() && count++ < limit) {
            var record = new ArrayList<String>();
            for (var index = 0; index < columns.size(); index++) {
                record.add(String.valueOf(rs.getObject(columns.get(index))));
                sizes.set(index, Math.max(sizes.get(index), record.getLast().length()));
            }
            records.add(record);
        }
        count = 0;
        var width = ctx.width();
        var pos = 0;
        while (pos < columns.size()) {
            if (count + sizes.get(pos) + 1 > width)
                break;
            count += sizes.get(pos++) + 1;
        }
        var header = columns.subList(0, pos);
        for (var index = 0; index < header.size(); index++)
            ctx.output("`*`%s`:` ", leftJust(header.get(index), sizes.get(index)));
        ctx.lineBreak();
        for (var index = 0; index < header.size(); index++)
            ctx.output("`*`%s`:` ", line(sizes.get(index)));
        ctx.lineBreak();
        for (var record : records) {
            for (var index = 0; index < header.size(); index++)
                ctx.output("%s ", leftJust(record.get(index), sizes.get(index)));
            ctx.lineBreak();
        }
    }
}
