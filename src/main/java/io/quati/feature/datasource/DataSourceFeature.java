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
            String user,
            String password) {

        public Map<String, String> toMap() {
            return Map.of(
                    "name", name,
                    "driver", driver,
                    "host", host,
                    "port", port,
                    "database", database,
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

    public void create(DataSource ds) {
        context.writeTextFile(ds.name, Json.toStr(ds.toMap()));
    }

    public DataSource find(String name) {
        if (!names().contains(name))
            return null;
        return DataSource.fromJson(context.readTextFile(name));
    }

    public void remove(String name) {
        context.deleteFile(name);
    }

    public Connection connect(String name) throws SQLException {
        var dataSource = find(name);
        if (dataSource == null)
            throw new InternalError("The DataSource %s do not exists.".formatted(name));
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