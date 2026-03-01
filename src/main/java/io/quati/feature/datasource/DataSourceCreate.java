package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Option;
import io.quati.api.Argument;
import io.quati.feature.driver.DriverFeature;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.util.List;

import static io.quati.api.Arity.ONE;
import static io.quati.api.Arity.ZERO_OR_ONE;
import static io.quati.feature.datasource.DataSourceFeature.*;

@Command(name = "create", description = "create datasource")
public class DataSourceCreate implements Action {

    @Argument(label = "NAME", desc = "name of the datasource to be created", arity = ONE)
    String name;

    @Option(name = "-d|--driver", desc = "name of the JDBC driver", label = "DRIVER", arity = ONE)
    String driver;

    @Option(name = "-H|--host", label = "HOST", desc = "database host", arity = ONE)
    String host;

    @Option(name = "-P|--port", label = "PORT", desc = "database port", arity = ZERO_OR_ONE)
    String port;

    @Option(name = "-D|--database", label = "DATABASE", desc = "database name", arity = ONE)
    String database;

    @Option(name = "-u|--user", label = "USER", desc = "database user name", arity = ONE)
    String user;

    @Option(name = "-p|--password", label = "PASSWORD", desc = "database user password", arity = ONE)
    String password;

    @Override
    public void completeOpt(Context ctx, String opt, String value, List<Candidate> candidates) {
        var installed = ctx.quati().feature(DriverFeature.class).installed();
        if (opt.equals("-d") && !installed.contains(value))
            installed.stream()
                    .map(Utils::candidate)
                    .forEach(candidates::add);
    }

    @Override
    public void execute(Context ctx) {
        var driverFeature = ctx.quati().feature(DriverFeature.class);
        if (driverFeature.installed().contains(driver)) {
            var datasource = ctx.quati().feature(DataSourceFeature.class);
            var dsPort = port == null
                    ? driverFeature.info(driver).defaultPort()
                    : port;
            datasource.write(new DataSource(name, driver, host, dsPort, database, user, password));
            ctx.output("DataSource `b`%s`:` created `g`successfully!`:`", name);
        } else
            ctx.output("The driver `r`%s`:` is not installed!%n", driver);
    }
}
