package io.quati.feature.driver;

import io.quati.api.Feature;
import io.quati.core.AbstractFeature;
import io.quati.feature.driver.vendor.DriverMSSQLServer;
import io.quati.feature.driver.vendor.DriverMySQL;
import io.quati.feature.driver.vendor.DriverOracle;
import io.quati.feature.driver.vendor.DriverPostgreSQL;

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

    private final Map<String, DriverVendor> driverMap = Map.of(
            "postgresql", new DriverPostgreSQL(),
            "oracle", new DriverOracle(),
            "mysql", new DriverMySQL(),
            "mssqlserver", new DriverMSSQLServer());

    public DriverVendor vendor(String driver) {
        return driverMap.get(driver);
    }

    public Set<String> available() {
        return driverMap.keySet();
    }

    public List<String> installed() {
        return context.fileNames(name -> name.replace(".jar", ""));
    }

    public void install(String driver) {
        context.startTarget(driver);
        var info = vendor(driver);
        if (info == null)
            context.error("`rr`driver not found: %s%n`:`", driver);
        else
            try (var client = HttpClient.newHttpClient()) {
                var request = HttpRequest.newBuilder()
                        .uri(info.mavenRepo())
                        .GET()
                        .build();
                var response = client.send(request, BodyHandlers.ofFile(context.file(driver + ".jar")));
                if (response.statusCode() == 200)
                    context.endTargetSuccessfully("Driver", driver, "installed");
                else
                    context.error("`r`failed – HTTP %s!`:`%n", response.statusCode());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    public void remove(String driver) {
        context.startTarget(driver);
        if (installed().contains(driver)) {
            context.deleteFile(driver + ".jar");
            context.endTargetSuccessfully("Driver", driver, "successfully");
        } else
            errorNotInstaled(driver);
    }

    public void errorNotInstaled(String driver) {
        context.output("driver `r`%s`:` is not installed!%n", driver);
    }

    public DriverFeature load(String driver) {
        try {
            var path = context
                    .repository()
                    .resolve(driver + ".jar");
            if (Files.notExists(path))
                errorNotInstaled(driver);
            var classLoader = DriverFeature.class.getClassLoader();
            var loader = new URLClassLoader(new URL[]{path.toUri().toURL()}, classLoader);
            var info = vendor(driver);
            if (info == null)
                throw new InternalError("driver info not found for driver '%s'.".formatted(driver));
            var clazz = Class.forName(info.driver, true, loader);
            var object = clazz.getDeclaredConstructor().newInstance();
            DriverManager.registerDriver(new DriverShim((Driver) object));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}