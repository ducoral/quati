package io.quati.feature.driver.vendor;

import io.quati.feature.driver.DriverVendor;

public class DriverOracle extends DriverVendor {

    public DriverOracle() {
        super(
                "oracle",
                "com/oracle/database/jdbc",
                "ojdbc11",
                "23.26.1.0.0",
                "1521",
                "oracle.jdbc.OracleDriver",
                "jdbc:oracle:thin:@//{host}:{port}/{database}");
    }

    @Override
    public String selectTable(String schema, String table, String columns, String condition, int limit) {
        var fetch = limit > 0
                ? " FETCH FIRST %d ROWS ONLY".formatted(limit)
                : "";
        var where = condition == null || condition.isBlank()
                ? ""
                : " WHERE %s".formatted(condition);
        return "SELECT %s FROM %s.%s%s%s".formatted(columns, schema, table, where, fetch);
    }
}
