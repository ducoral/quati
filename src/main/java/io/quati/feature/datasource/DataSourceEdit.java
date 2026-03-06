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

@Command(name = "edit", description = "edit datasource")
public class DataSourceEdit implements Action {

    @Argument(label = "NAME", description = "name of the datasource to be edited", arity = ONE)
    String name;

    @Option(name = "-d|--driver", description = "set the JDBC driver", label = "DRIVER")
    String driver;

    @Option(name = "-H|--host", label = "HOST", description = "set database host")
    String host;

    @Option(name = "-P|--port", label = "PORT", description = "set database port")
    String port;

    @Option(name = "-D|--database", label = "DATABASE", description = "set database name")
    String database;

    @Option(name = "-s|--schema", label = "SCHEMA", description = "set database schema")
    String schema;

    @Option(name = "-u|--user", label = "USER", description = "set database user name")
    String user;

    @Option(name = "-p|--password", label = "PASSWORD", description = "set database user password")
    String password;

    @Override
    public void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
        Utils.completeCandidates(ctx.datasource().names(), value, null, candidates, true);
    }

    @Override
    public void completeOpt(Context ctx, String opt, String value, List<Candidate> candidates) {
        if (opt.equals("-d"))
            Utils.completeCandidates(ctx.driver().installed(), value, null, candidates, true);
    }

    @Override
    public void execute(Context ctx) {
        ctx.startTarget(name);
        var feature = ctx.datasource();
        var ds = feature.find(name);
        if (ds == null) {
            ctx.errorNotExists("datasource", name);
            return;
        }
        if (driver == null
                && host == null
                && port == null
                && database == null
                && schema == null
                && user == null
                && password == null) {
            ctx.error("`yy`please provide an attribute to be edited`:`%n");
            return;
        }
        var driverFeature = ctx.driver();
        if (driver != null && !driverFeature.installed().contains(driver)) {
            driverFeature.errorNotInstaled(driver);
            return;
        }
        var edited = new DataSource(name,
                driver == null ? ds.driver() : driver,
                host == null ? ds.host() : host,
                port == null ? ds.port() : port,
                database == null ? ds.database() : database,
                schema == null ? ds.schema() : schema,
                user == null ? ds.user() : user,
                password == null ? ds.password() : password);
        feature.write(edited);
        ctx.endTargetSuccessfully("datasource", name, "edited");
        feature.print(edited, false);
    }
}
