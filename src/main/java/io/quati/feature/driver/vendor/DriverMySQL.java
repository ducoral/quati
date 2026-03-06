package io.quati.feature.driver.vendor;

import io.quati.feature.driver.DriverVendor;

public class DriverMySQL extends DriverVendor {

    public DriverMySQL() {
        super(
                "mysql",
                "com/mysql",
                "mysql-connector-j",
                "9.6.0",
                "3306",
                "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://{host}:{port}/{database}");
    }

    @Override
    public String selectTable(String schema, String table, String columns, String condition, int limit) {
        var lim = limit > 0
                ? " LIMIT " + limit
                : "";
        var where = condition == null || condition.isBlank()
                ? ""
                : " WHERE %s".formatted(condition);
        return "SELECT %s FROM %s.%s%s%s".formatted(columns, schema, table, where, lim);
    }
}
