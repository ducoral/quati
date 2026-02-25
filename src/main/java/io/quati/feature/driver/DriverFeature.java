package io.quati.feature.driver;

import io.quati.api.Context;
import io.quati.api.Feature;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Feature(
        name = "driver",
        desc = "JDBC driver manager",
        commands = {
                DriverList.class,
                DriverInstall.class,
                DriverRemove.class
        })
public class DriverFeature {

    private static final String MAVEN_URL = "https://repo1.maven.org/maven2/%s/%s/%s/%s-%s.jar";

    private static final List<String> DRIVER_STRS = List.of(
            "postgresql|org/postgresql|postgresql|42.7.4|org.postgresql.Driver",
            "oracle|com/oracle/database/jdbc|ojdbc8|23.26.1.0.0|oracle.jdbc.OracleDriver",
            "mysql|com/mysql|mysql-connector-j|9.6.0|com.mysql.cj.jdbc.Driver",
            "mssqlserver|com/microsoft/sqlserver|mssql-jdbc|13.3.1.jre11-preview|com.microsoft.sqlserver.jdbc.SQLServerDriver"
    );

    private static String getDriverStr(String driver) {
        for (var driverStr : DRIVER_STRS)
            if (driverStr.startsWith(driver + "|"))
                return driverStr;
        return null;
    }

    private static URI downloadURI(String driverStr) {
        var driver = driverStr.split("\\|");
        return URI.create(
                MAVEN_URL.formatted(driver[1], driver[2], driver[3], driver[2], driver[3]));
    }

    public static List<String> getAll() {
        return DRIVER_STRS
                .stream()
                .map(d -> d.substring(0, d.indexOf("|")))
                .toList();
    }

    public static List<String> getInstalled(Context ctx) {
        var list = new ArrayList<String>();
        ctx.files(path -> {
            var driverName = path
                    .getFileName()
                    .toString()
                    .replace(".jar", "");
            list.add(driverName);
        });
        return list;
    }

    public static void install(Context ctx, String driver) {
        var driverStr = getDriverStr(driver);
        if (driverStr == null)
            ctx.error(":rr:Driver not found: %s%n::", driver);
        else
            try (var client = HttpClient.newHttpClient()) {
                var request = HttpRequest.newBuilder()
                        .uri(downloadURI(driverStr))
                        .GET()
                        .build();
                var pathToSave = ctx
                        .repository()
                        .resolve(driver + ".jar");
                var response = client.send(request, BodyHandlers.ofFile(pathToSave));
                if (response.statusCode() == 200)
                    ctx.output("The :gg:%s:: driver was installed successfully!%n", driver);
                else
                    ctx.error(":r:Failed – HTTP %s!::%n", response.statusCode());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    public static void remove(Context ctx, String driver) {
        if (!getInstalled(ctx).contains(driver))
            ctx.error(":r:The driver '%s' is not installed!%n", driver);
        else try {
            var path = ctx
                    .repository()
                    .resolve(driver + ".jar");
            Files.delete(path);
            ctx.output("The :gg:%s:: driver was successfully removed!%n", driver);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void load(String driver) {
        try {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}