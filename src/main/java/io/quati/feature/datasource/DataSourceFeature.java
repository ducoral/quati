package io.quati.feature.datasource;

import io.quati.api.Feature;
import io.quati.core.AbstractFeature;
import io.quati.feature.driver.DriverFeature;
import io.quati.util.Json;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Feature(
        name = "datasource",
        description = "JDBC connection configuration",
        commands = {
                DataSourceList.class,
                DataSourceCreate.class,
                DataSourceCopy.class,
                DataSourceEdit.class,
                DataSourceTest.class,
                DataSourceInfo.class,
                DataSourceRemove.class
        })
public class DataSourceFeature extends AbstractFeature {

    public record DataSource(
            String name,
            String driver,
            String host,
            String port,
            String database,
            String schema,
            String user,
            String password) {

        public Map<String, String> toMap() {
            return Map.of(
                    "name", name,
                    "driver", driver,
                    "host", host,
                    "port", port,
                    "database", database,
                    "schema", schema,
                    "user", user,
                    "password", password);
        }

        public static DataSource fromMap(Map<String, String> map) {
            return new DataSource(
                    map.get("name"),
                    map.get("driver"),
                    map.get("host"),
                    map.get("port"),
                    map.get("database"),
                    map.get("schema"),
                    map.get("user"),
                    map.get("password"));
        }

        @SuppressWarnings("unchecked")
        public static DataSource fromJson(String json) {
            return fromMap((Map<String, String>) Json.parse(json));
        }
    }

    public List<String> names() {
        return context.fileNames();
    }

    public List<DataSource> dataSources() {
        return names().stream()
                .map(name -> DataSource.fromJson(context.readTextFile(name)))
                .toList();
    }

    public DataSource write(DataSource ds) {
        context.writeTextFile(ds.name, Json.toStr(ds.toMap()));
        return ds;
    }

    public DataSource find(String name) {
        if (!names().contains(name))
            return null;
        return DataSource.fromJson(context.readTextFile(name));
    }

    public DataSource copy(String source, String destination) {
        var ds = find(source);
        if (ds == null)
            return null;
        return write(new DataSource(destination, ds.driver, ds.host, ds.port, ds.database, ds.schema, ds.user, ds.password));
    }

    public void remove(String name) {
        context.deleteFile(name);
    }

    public void print(DataSource ds, boolean showPassword) {
        context.output("`b`name     :`:` %s%n", ds.name());
        context.output("`b`driver   :`:` %s%n", ds.driver());
        context.output("`b`host     :`:` %s%n", ds.host());
        context.output("`b`port     :`:` %s%n", ds.port());
        context.output("`b`database :`:` %s%n", ds.database());
        context.output("`b`schema   :`:` %s%n", ds.schema());
        context.output("`b`user     :`:` %s%n", ds.user());
        context.output("`b`password :`:` %s%n", showPassword ? ds.password() : "*".repeat(ds.password().length()));
    }

    public void errorNotExists(String name) {
        context.error("The datasource `r`%s`:` do not exists!%n", name);
    }

    public Connection connect(String name) throws SQLException {
        var dataSource = find(name);
        if (dataSource == null)
            throw new InternalError("The datasource %s do not exists.".formatted(name));
        var driver = context
                .quati()
                .feature(DriverFeature.class)
                .load(dataSource.driver)
                .info(dataSource.driver);
        return DriverManager.getConnection(
                driver.jdbcURL(dataSource.host, dataSource.port, dataSource.database),
                dataSource.user,
                dataSource.password);
    }
}