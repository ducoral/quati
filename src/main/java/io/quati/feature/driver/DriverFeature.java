package io.quati.feature.driver;

import io.quati.api.Feature;
import io.quati.core.AbstractFeature;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

@Feature(
        name = "driver",
        description = "JDBC driver manager",
        commands = {
                DriverList.class,
                DriverInstall.class,
                DriverRemove.class,
                DriverLoad.class
        })
public class DriverFeature extends AbstractFeature {

    public record DriverInfo(String name, String group, String artifactId, String version, String driverClass) {
        public static DriverInfo of(String driverString) {
            var array = driverString.split("\\|");
            if (array.length != 5)
                throw new RuntimeException("Invalid Driver String '%s'.".formatted(driverString));
            return new DriverInfo(array[0], array[1], array[2], array[3], array[4]);
        }
    }

    private static final String MAVEN_URL = "https://repo1.maven.org/maven2/%s/%s/%s/%s-%s.jar";

    private static final List<String> DRIVER_STRINGS = List.of(
            "postgresql|org/postgresql|postgresql|42.7.10|org.postgresql.Driver",
            "oracle|com/oracle/database/jdbc|ojdbc8|23.26.1.0.0|oracle.jdbc.OracleDriver",
            "mysql|com/mysql|mysql-connector-j|9.6.0|com.mysql.cj.jdbc.Driver",
            "mssqlserver|com/microsoft/sqlserver|mssql-jdbc|13.3.1.jre11-preview|com.microsoft.sqlserver.jdbc.SQLServerDriver"
    );

    private DriverInfo driverInfo(String driver) {
        for (var driverString : DRIVER_STRINGS)
            if (driverString.startsWith(driver + "|"))
                return DriverInfo.of(driverString);
        return null;
    }

    private URI downloadURI(DriverInfo info) {
        return URI.create(
                MAVEN_URL.formatted(
                        info.group,
                        info.artifactId,
                        info.version,
                        info.artifactId,
                        info.version));
    }

    public List<String> getAll() {
        return DRIVER_STRINGS
                .stream()
                .map(d -> d.substring(0, d.indexOf("|")))
                .toList();
    }

    public List<String> getInstalled() {
        var list = new ArrayList<String>();
        context.files(path -> {
            var driverName = path
                    .getFileName()
                    .toString()
                    .replace(".jar", "");
            list.add(driverName);
        });
        return list;
    }

    public void install(String driver) {
        var driverStr = driverInfo(driver);
        if (driverStr == null)
            context.error("`rr`Driver not found: %s%n`:`", driver);
        else
            try (var client = HttpClient.newHttpClient()) {
                var request = HttpRequest.newBuilder()
                        .uri(downloadURI(driverStr))
                        .GET()
                        .build();
                var pathToSave = context
                        .repository()
                        .resolve(driver + ".jar");
                var response = client.send(request, BodyHandlers.ofFile(pathToSave));
                if (response.statusCode() == 200)
                    context.output("The `gg`%s`:` driver was installed successfully!%n", driver);
                else
                    context.error("`r`Failed – HTTP %s!`:`%n", response.statusCode());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    public void remove(String driver) {
        if (!getInstalled().contains(driver))
            context.error("`r`The driver '%s' is not installed!`:`%n", driver);
        else try {
            var path = context
                    .repository()
                    .resolve(driver + ".jar");
            Files.delete(path);
            context.output("The `gg`%s`:` driver was successfully removed!%n", driver);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load(String driver) {
        try {
            var path = context
                    .repository()
                    .resolve(driver + ".jar");
            if (Files.notExists(path))
                context.output("The `r`%s`:` driver is not installed!%n", driver);

            URL[] urls = {path.toUri().toURL()};
            var classLoader = DriverFeature.class.getClassLoader();
            var loader = new URLClassLoader(urls, classLoader);
            var info = driverInfo(driver);
            if (info == null)
                throw new InternalError("DriverInfo not found for driver '%s'".formatted(driver));
            var clazz = Class.forName(info.driverClass, true, loader);
            var object = clazz.getDeclaredConstructor().newInstance();
            DriverManager.registerDriver(new DriverShim((Driver) object));
        } catch (Exception e) {
            context.error("`r`Error: %s`:`%n", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}