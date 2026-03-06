package io.quati.feature.schema;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Arity;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Flag;
import io.quati.api.Option;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.quati.feature.schema.SchemaFeature.Column;
import static io.quati.feature.schema.SchemaFeature.Relation;
import static io.quati.feature.schema.SchemaFeature.Schema;
import static io.quati.util.Utils.leftJust;
import static io.quati.util.Utils.line;
import static io.quati.util.Utils.rightJust;

@Command(name = "desc", description = "describes schema items")
public class SchemaDesc implements Action {

    @Argument(label = "SCHEMA", description = "scheme that will be described", arity = Arity.ONE)
    private String schemaName;

    @Option(name = "-t|--table", label = "TABLE", description = "table to be described")
    private String tableName;

    @Flag(name = "-o|--only-columns", description = "option to show only column description")
    private boolean onlyColumns;

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
        else if (schema.tables().containsKey(tableName))
            descTable(schema, ctx);
        else
            descSchemaTables(schema, ctx);
    }

    private void descTable(Schema schema, Context ctx) {
        var table = schema.tables().get(tableName);
        int[] c = {calcSize(table.columns().stream(), 6, Column::name), 11, 10, 6, 8, 7};
        ctx.output("`*`table columns:`:`%n");
        ctx.output("  `*`%s  %s  %s  %s  %s  %s`:`%n",
                leftJust("column", c[0]),
                leftJust("type", c[1]),
                rightJust("size", c[2]),
                rightJust("digits", c[3]),
                leftJust("nullable", c[4]),
                rightJust("ordinal", c[5]));
        ctx.output("  `*`%s  %s  %s  %s  %s  %s`:`%n", line(c[0]), line(c[1]), line(c[2]), line(c[3]), line(c[4]), line(c[5]));
        for (var column : table.columns()) {
            var color = ":";
            if (table.isExported(column.name()))
                color = "c";
            else if (table.isImported(column.name()))
                color = "b";
            ctx.output("  `%s`%s`:`  %s  %s  %s  %s  %s`:`%n",
                    color,
                    leftJust(column.name(), c[0]),
                    leftJust(typeName(column.type()), c[1]),
                    rightJust(String.valueOf(column.size()), c[2]),
                    rightJust(String.valueOf(column.digits()), c[3]),
                    leftJust(nullStr(column.nullable()), c[4]),
                    rightJust(String.valueOf(column.ordinal()), c[5]));
        }
        if (!onlyColumns) {
            descRelation(table.exported(), "exported", "c", ctx);
            descRelation(table.imported(), "imported", "b", ctx);
        }
    }

    private void descRelation(List<Relation> relations, String label, String color, Context ctx) {
        if (relations.isEmpty())
            return;
        int[] cols = {
                calcSize(relations.stream(), 7, Relation::lsColumn),
                calcSize(relations.stream(), 15, Relation::rsTable),
                calcSize(relations.stream(), 15, Relation::rsColumn)};
        ctx.output("`*`%s columns:`:`%n", label);
        ctx.output("  `*`%s  %s  %s`:`%n",
                leftJust("column", cols[0]),
                leftJust("%s table".formatted(label), cols[1]),
                leftJust("%s column".formatted(label), cols[2]));
        ctx.output("  `*`%s  %s  %s`:`%n", line(cols[0]), line(cols[1]), line(cols[2]));
        for (var relation : relations) {
            ctx.output("  `%s`%s`:`  %s  %s%n",
                    color,
                    leftJust(relation.lsColumn(), cols[0]),
                    leftJust(relation.rsTable(), cols[1]),
                    leftJust(relation.rsColumn(), cols[2]));
        }
    }

    private <T> int calcSize(Stream<T> stream, int minSize, Function<T, String> valueFunc) {
        var size = stream
                .map(valueFunc)
                .map(String::length)
                .reduce(0, Math::max);
        return Math.max(minSize, size);
    }

    private void descSchemaTables(Schema schema, Context ctx) {
        int colSize = calcSize(schema.tables().keySet().stream(), 5, s -> s);
        int cols = ctx.width() / colSize;
        int col = 0;
        for (var table : schema.tables().keySet()) {
            if (col == cols) {
                ctx.lineBreak();
                col = 0;
            }
            ctx.output("%s ", leftJust(table, colSize));
            col++;
        }
        ctx.lineBreak();
    }

    private String nullStr(int nullable) {
        return nullable == 0
                ? "not null"
                : "null";
    }

    private String typeName(int type) {
        try {
            return JDBCType.valueOf(type).toString();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}