package io.quati.feature.driver;

import io.quati.api.Feature;
import io.quati.core.AbstractFeature;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public record DriverInfo(
            String group,
            String artifact,
            String version,
            String defaultPort,
            String driver,
            String jdbcURL) {

        public URI mavenRepo() {
            var url = "https://repo1.maven.org/maven2/{group}/{artifact}/{version}/{artifact}-{version}.jar"
                    .replace("{group}", group)
                    .replace("{artifact}", artifact)
                    .replace("{version}", version);
            return URI.create(url);
        }

        public String jdbcURL(String host, String port, String database) {
            return jdbcURL
                    .replace("{host}", host)
                    .replace("{port}", port)
                    .replace("{database}", database);
        }
    }

    private final Map<String, DriverInfo> driverMap = Map.of(
            "postgresql", new DriverInfo(
                    "org/postgresql",
                    "postgresql",
                    "42.7.10",
                    "5432",
                    "org.postgresql.Driver",
                    "jdbc:postgresql://{host}:{port}/{database}"),
            "oracle", new DriverInfo(
                    "com/oracle/database/jdbc",
                    "ojdbc8",
                    "23.26.1.0.0",
                    "1521",
                    "oracle.jdbc.OracleDriver",
                    "jdbc:oracle:thin:@//{host}:{port}/{database}"),
            "mysql", new DriverInfo(
                    "com/mysql",
                    "mysql-connector-j",
                    "9.6.0",
                    "3306",
                    "com.mysql.cj.jdbc.Driver",
                    "jdbc:mysql://{host}:{port}/{database}"),
            "mssqlserver", new DriverInfo(
                    "com/microsoft/sqlserver",
                    "mssql-jdbc",
                    "13.3.1.jre11-preview",
                    "1433",
                    "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                    "jdbc:sqlserver://{host}:{port};databaseName={database}"));

    public DriverInfo info(String driver) {
        return driverMap.get(driver);
    }

    public Set<String> available() {
        return driverMap.keySet();
    }

    public List<String> installed() {
        return context.fileNames(name -> name.replace(".jar", ""));
    }

    public void install(String driver) {
        var info = info(driver);
        if (info == null)
            context.error("`rr`Driver not found: %s%n`:`", driver);
        else
            try (var client = HttpClient.newHttpClient()) {
                var request = HttpRequest.newBuilder()
                        .uri(info.mavenRepo())
                        .GET()
                        .build();
                var response = client.send(request, BodyHandlers.ofFile(context.file(driver + ".jar")));
                if (response.statusCode() == 200)
                    context.output("The `gg`%s`:` driver was installed successfully!%n", driver);
                else
                    context.error("`r`Failed – HTTP %s!`:`%n", response.statusCode());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    public void remove(String driver) {
        if (installed().contains(driver))
            context.deleteFile(driver + ".jar");
        else
            context.error("`r`The driver '%s' is not installed!`:`%n", driver);
    }

    public DriverFeature load(String driver) {
        try {
            var path = context
                    .repository()
                    .resolve(driver + ".jar");
            if (Files.notExists(path))
                context.output("The `r`%s`:` driver is not installed!%n", driver);
            var classLoader = DriverFeature.class.getClassLoader();
            var loader = new URLClassLoader(new URL[]{path.toUri().toURL()}, classLoader);
            var info = info(driver);
            if (info == null)
                throw new InternalError("DriverInfo not found for driver '%s'.".formatted(driver));
            var clazz = Class.forName(info.driver, true, loader);
            var object = clazz.getDeclaredConstructor().newInstance();
            DriverManager.registerDriver(new DriverShim((Driver) object));
        } catch (Exception e) {
            context.error("`r`Error: %s`:`%n", e.getMessage());
            throw new RuntimeException(e);
        }
        return this;
    }
}