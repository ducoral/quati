package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;

import java.sql.DriverManager;
import java.sql.SQLException;

import static io.quati.api.Arity.ONE;

@Command(name = "test", description = "test datasource connection")
public class DataSourceTest implements Action {

    @Argument(label = "NAME", desc = "name of the datasource to be tested", arity = ONE)
    String datasource;

    @Override
    public void execute(Context ctx) {
        if (!datasource.equals("myDS")) {
            ctx.error("`r`DataSource `%s` is not configured!`:`%n", datasource);
            return;
        }
        try (var conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/alicerce", "test", "test")) {
            ctx.output("The DataSource `bb`%s`:` connected `bb`successfully!`:`%n", datasource);
        } catch (SQLException e) {
            ctx.error("`r`Error: %s`:`%n", e.getMessage());
        }
    }
}
