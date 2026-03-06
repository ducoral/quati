package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Option;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.util.List;

import static io.quati.api.Arity.ONE;
import static io.quati.feature.datasource.DataSourceFeature.DataSource;

@Command(name = "create", description = "create datasource")
public class DataSourceCreate implements Action {

    @Argument(label = "NAME", description = "name of the datasource to be created", arity = ONE)
    String name;

    @Option(name = "-d|--driver", description = "name of the JDBC driver", label = "DRIVER", arity = ONE)
    String driver;

    @Option(name = "-H|--host", label = "HOST", description = "database host", arity = ONE)
    String host;

    @Option(name = "-P|--port", label = "PORT", description = "database port")
    String port;

    @Option(name = "-D|--database", label = "DATABASE", description = "database name", arity = ONE)
    String database;

    @Option(name = "-s|--schema", label = "SCHEMA", description = "database schema", arity = ONE)
    String schema;

    @Option(name = "-u|--user", label = "USER", description = "database user name", arity = ONE)
    String user;

    @Option(name = "-p|--password", label = "PASSWORD", description = "database user password", arity = ONE)
    String password;

    @Override
    public void completeOpt(Context ctx, String opt, String value, List<Candidate> candidates) {
        var installed = ctx.driver().installed();
        if (opt.equals("-d") && !installed.contains(value))
            installed.stream()
                    .map(Utils::candidate)
                    .forEach(candidates::add);
    }

    @Override
    public void execute(Context ctx) {
        ctx.startTarget(name);
        var driverFeature = ctx.driver();
        if (driverFeature.installed().contains(driver)) {
            var datasource = ctx.datasource();
            var dsPort = port == null
                    ? driverFeature.vendor(driver).port
                    : port;
            datasource.write(new DataSource(name, driver, host, dsPort, database, schema, user, password));
            ctx.endTargetSuccessfully("datasource", name, "created");
        } else
            driverFeature.errorNotInstaled(driver);
    }
}
