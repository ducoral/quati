package io.quati.feature.schema;

import io.quati.api.Feature;
import io.quati.core.AbstractFeature;
import io.quati.util.Json;
import io.quati.util.Utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Feature(
        name = "schema",
        description = "datasource database schema",
        commands = {
                SchemaUpdate.class
        })
public class SchemaFeature extends AbstractFeature {

    public record Relation(String leftSideColumn, String rightSideTable, String rightSideColumn) {
        public Map<?, ?> toMap() {
            return Map.of(
                    "l", leftSideColumn,
                    "t", rightSideTable,
                    "r", rightSideColumn);
        }

        public static Relation fromMap(Map<?, ?> map) {
            return new Relation(
                    (String) map.get("l"),
                    (String) map.get("t"),
                    (String) map.get("r"));
        }

        public static Relation fromExportedKeys(ResultSet rs) throws SQLException {
            return new Relation(
                    rs.getString("PKCOLUMN_NAME"),
                    rs.getString("FKTABLE_NAME"),
                    rs.getString("FKCOLUMN_NAME"));
        }

        public static Relation fromImportedKeys(ResultSet rs) throws SQLException {
            return new Relation(
                    rs.getString("FKCOLUMN_NAME"),
                    rs.getString("PKTABLE_NAME"),
                    rs.getString("PKCOLUMN_NAME"));
        }
    }

    public record Column(String name, int type, int size, int digits, int nullable, int ordinal) {
        public Map<?, ?> toMap() {
            return Map.of(
                    "n", name,
                    "t", type,
                    "s", size,
                    "d", digits,
                    "l", nullable,
                    "o", ordinal);
        }

        public static Column fromMap(Map<?, ?> map) {
            return new Column(
                    (String) map.get("n"),
                    (int) map.get("t"),
                    (int) map.get("s"),
                    (int) map.get("d"),
                    (int) map.get("l"),
                    (int) map.get("o"));
        }

        public static Column fromResultSet(ResultSet rs) throws SQLException {
            return new Column(
                    rs.getString("COLUMN_NAME"),
                    rs.getInt("DATA_TYPE"),
                    rs.getInt("COLUMN_SIZE"),
                    rs.getInt("DECIMAL_DIGITS"),
                    rs.getInt("NULLABLE"),
                    rs.getInt("ORDINAL_POSITION"));
        }
    }

    public record Table(List<Column> columns, List<Relation> exported, List<Relation> imported) {
        public Map<?, ?> toMap() {
            return Map.of(
                    "c", Utils.toListOfMap(columns, Column::toMap),
                    "e", Utils.toListOfMap(exported, Relation::toMap),
                    "i", Utils.toListOfMap(imported, Relation::toMap));
        }

        public static Table fromMap(Map<?, ?> map) {
            return new Table(
                    Utils.getAsListOf(map, "c", Column::fromMap),
                    Utils.getAsListOf(map, "e", Relation::fromMap),
                    Utils.getAsListOf(map, "i", Relation::fromMap));
        }
    }

    public record Schema(Map<String, Table> tables) {
        public Map<?, ?> toMap() {
            var map = new HashMap<>();
            tables.forEach((name, table) -> map.put(name, table.toMap()));
            return Map.of("tables", map);
        }

        public static Schema fromMap(Map<?, ?> map) {
            var tables = new HashMap<String, Table>();
            ((Map<?, ?>) map.get("tables"))
                    .forEach((name, table)
                            -> tables.put((String) name, Table.fromMap((Map<?, ?>) table)));
            return new Schema(tables);
        }

        @SuppressWarnings("unchecked")
        public static Schema fromJson(String json) {
            return fromMap((Map<String, String>) Json.parse(json));
        }
    }

    public void update(String name) {
        var datasource = context.datasource().find(name);
        if (datasource == null)
            context.datasource().errorNotExists(name);
        else try (var conn = context.datasource().connect(name)) {
            var schema = new Schema(new HashMap<>());
            var metaData = conn.getMetaData();
            var tables = metaData.getTables(null, datasource.schema(), null, new String[]{"TABLE"});
            while (tables.next()) {
                var table = new Table(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                var tableName = tables.getString("TABLE_NAME");
                context.output("%s ", tableName);
                var columns = metaData.getColumns(null, datasource.schema(), tableName, null);
                while (columns.next()) {
                    context.output("`y`.`:`");
                    table.columns.add(Column.fromResultSet(columns));
                }
                var exported = metaData.getExportedKeys(null, datasource.schema(), tableName);
                while (exported.next()) {
                    context.output("`c`.`:`");
                    table.exported.add(Relation.fromExportedKeys(exported));
                }
                var imported = metaData.getImportedKeys(null, datasource.schema(), tableName);
                while (imported.next()) {
                    context.output("`b`.`:`");
                    table.imported.add(Relation.fromImportedKeys(imported));
                }
                schema.tables.put(tableName, table);
                context.output("%n");
            }

            context.schema().write(name, schema);
            context.outputSuccessfully("Schema", name, "updated");
        } catch (SQLException e) {
            context.error(e);
        }
    }

    public List<String> names() {
        return context.fileNames();
    }

    public void write(String name, Schema schema) {
        context.writeTextFile(name, Json.toStr(schema.toMap()));
    }

    public Schema find(String name) {
        if (!names().contains(name))
            return null;
        return Schema.fromJson(context.readTextFile(name));
    }
}

