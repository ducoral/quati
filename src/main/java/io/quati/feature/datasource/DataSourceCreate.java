package io.quati.feature.datasource;

import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.api.Option;
import io.quati.api.Position;

import static io.quati.api.Arity.ONE;
import static io.quati.api.Arity.ZERO_OR_ONE;

public class DataSourceCreate implements Command {

    @Position(label = "NAME", desc = "name of the datasource to be created", arity = ONE)
    String datasource;

    @Option(names = "-d|--driver", desc = "name of the installed driver", label = "DRIVER", arity = ONE)
    String driver;

    @Option(names = "-H|--host", label = "HOST", desc = "database host", arity = ONE)
    String host;

    @Option(names = "-P|--port", label = "PORT", desc = "database port", arity = ZERO_OR_ONE)
    String port;

    @Option(names = "-D|--database", label = "DATABASE", desc = "database name", arity = ONE)
    String database;

    @Option(names = "-u|--user-name", label = "USER", desc = "database user name", arity = ONE)
    String userName;

    @Option(names = "-p|--password", label = "PASSWORD", desc = "database user password", arity = ONE)
    String password;

    @Override
    public String name() {
        return "create";
    }

    @Override
    public String desc() {
        return "create datasource";
    }

    @Override
    public void exec(Context ctx) {

    }
}
