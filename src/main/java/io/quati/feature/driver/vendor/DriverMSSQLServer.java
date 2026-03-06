package io.quati.feature.driver.vendor;

import io.quati.feature.driver.DriverVendor;

public class DriverMSSQLServer extends DriverVendor {

    public DriverMSSQLServer() {
        super(
                "mssqlserver",
                "com/microsoft/sqlserver",
                "mssql-jdbc",
                "13.2.1.jre11",
                "1433",
                "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                "jdbc:sqlserver://{host}:{port};databaseName={database};trustServerCertificate=true");
    }

    @Override
    public String selectTable(String schema, String table, String columns, String condition, int limit) {
        var top = limit > 0
                ? "TOP " + limit
                  : "";
        var where = condition == null || condition.isBlank()
                ? ""
                : " WHERE %s".formatted(condition);
        return "SELECT %s %s FROM %s.%s%s".formatted(top, columns, schema, table, where);
    }
}
