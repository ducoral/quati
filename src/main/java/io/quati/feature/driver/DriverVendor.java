package io.quati.feature.driver;

import java.net.URI;

public abstract class DriverVendor {
    public final String name;
    public final String group;
    public final String artifact;
    public final String version;
    public final String port;
    public final String driver;
    public final String jdbcURL;

    protected DriverVendor(
            String name,
            String group,
            String artifact,
            String version,
            String port,
            String driver,
            String jdbcURL) {
        this.name = name;
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.port = port;
        this.driver = driver;
        this.jdbcURL = jdbcURL;
    }

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

    public abstract String selectTable(String schema, String table, String columns, String condition, int limit);
}
