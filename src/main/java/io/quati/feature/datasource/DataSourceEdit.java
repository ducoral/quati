package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Option;
import io.quati.feature.driver.DriverFeature;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.util.List;

import static io.quati.api.Arity.ONE;
import static io.quati.api.Arity.ZERO_OR_ONE;
import static io.quati.feature.datasource.DataSourceFeature.DataSource;

@Command(name = "edit", description = "edit datasource")
public class DataSourceEdit implements Action {

    @Argument(label = "NAME", desc = "name of the datasource to be edited", arity = ONE)
    String name;

    @Option(name = "-d|--driver", desc = "set the JDBC driver", label = "DRIVER", arity = ZERO_OR_ONE)
    String driver;

    @Option(name = "-H|--host", label = "HOST", desc = "set database host", arity = ZERO_OR_ONE)
    String host;

    @Option(name = "-P|--port", label = "PORT", desc = "set database port", arity = ZERO_OR_ONE)
    String port;

    @Option(name = "-D|--database", label = "DATABASE", desc = "set database name", arity = ZERO_OR_ONE)
    String database;

    @Option(name = "-u|--user", label = "USER", desc = "set database user name", arity = ZERO_OR_ONE)
    String user;

    @Option(name = "-p|--password", label = "PASSWORD", desc = "set database user password", arity = ZERO_OR_ONE)
    String password;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        var names = ctx.quati().feature(DataSourceFeature.class).names();
        if (!names.contains(value))
            names.stream()
                    .map(Utils::candidate)
                    .forEach(candidates::add);
    }

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
        var feature = ctx.quati().feature(DataSourceFeature.class);
        var ds = feature.find(name);
        if (ds == null) {
            ctx.error("The DataSource `r`%s`:` do not exists!%n", feature);
            return;
        }
        if (driver == null && host == null && port == null && database == null && user == null && password == null) {
            ctx.error("`yy`Please provide an attribute to be edited`:`%n");
            return;
        }
        var driverFeature = ctx.quati().feature(DriverFeature.class);
        if (driver != null && !driverFeature.installed().contains(driver)) {
            ctx.output("The driver `r`%s`:` is not installed!%n", driver);
            return;
        }
        var edited = new DataSource(name,
                driver == null ? ds.driver() : driver,
                host == null ? ds.host() : host,
                port == null ? ds.port() : port,
                database == null ? ds.database() : database,
                user == null ? ds.user() : user,
                password == null ? ds.password() : password);
        feature.write(edited);
        ctx.output("DataSource `b`%s`:` edited `g`successfully!`:`%n", name);
        feature.print(edited, false);
    }
}
