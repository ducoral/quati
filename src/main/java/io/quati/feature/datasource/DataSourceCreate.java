package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Option;
import io.quati.api.Argument;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.util.List;

import static io.quati.api.Arity.ONE;
import static io.quati.api.Arity.ZERO_OR_ONE;

@Command(name = "create", desc = "create datasource")
public class DataSourceCreate implements Action {

    @Argument(label = "NAME", desc = "name of the datasource to be created", arity = ONE)
    String datasource;

    @Option(name = "-d|--driver", desc = "name of the JDBC driver", label = "DRIVER", arity = ONE)
    String driver;

    @Option(name = "-H|--host", label = "HOST", desc = "database host", arity = ONE)
    String host;

    @Option(name = "-P|--port", label = "PORT", desc = "database port", arity = ZERO_OR_ONE)
    String port;

    @Option(name = "-D|--database", label = "DATABASE", desc = "database name", arity = ONE)
    String database;

    @Option(name = "-u|--user-name", label = "USER", desc = "database user name", arity = ONE)
    String userName;

    @Option(name = "-p|--password", label = "PASSWORD", desc = "database user password", arity = ONE)
    String password;

    @Override
    public void completeOpt(Context ctx, String opt, String value, List<Candidate> candidates) {
        var installedDrivers = List.of("postgresql", "mysql", "oracle", "mssqlserver", "db2");

        if (opt.equals("-d") && !installedDrivers.contains(value))
            installedDrivers.stream()
                    .map(Utils::candidate)
                    .forEach(candidates::add);
    }

    @Override
    public void execute(Context ctx) {
        ctx.output("""
                datasource: %s
                driver: %s
                host: %s
                port: %s
                database: %s
                userName: %s
                password: %s
                """, datasource, driver, host, port, database, userName, password);
    }
}
