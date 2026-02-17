package io.quati.cmd;

import io.quati.cli.Command;
import io.quati.cli.Quati;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataSourceCommand implements Command {

    @Override
    public String name() {
        return "datasource";
    }

    @Override
    public String description() {
        return "JDBC connection configuration";
    }

    @Override
    public void configure(Builder builder) {
        builder
                .action("list", "list datasources");
        builder
                .action("create", "create datasource")
                .argument(true, "NAME", "name of the datasource that may be referenced later")
                .parameter(true, "-d", "--driver", "DRIVER", "name of the installed driver")
                .parameter(true, "-h", "--host", "HOST", "database host")
                .parameter(false, "-p", "--port", "PORT", "database port")
                .parameter(true, "-db", "--database", "DATABASE", "database name")
                .parameter(true, "-u", "--user-name", "USER", "database user name")
                .parameter(true, "-s", "--secret", "PASSWORD", "database user password");
        builder
                .action("test", "test datasource connection")
                .argument(true, "NAME", "name of the datasource to be tested");
        builder
                .action("info", "displays the datasource configuration")
                .argument(true, "NAME", "name of the datasource to be displayed");
        builder
                .action("remove", "remove the datasource")
                .argument(true, "NAME", "name of the datasource to be removed");
    }

    @Override
    public void completion(Quati quati, String argument, String completionWord, List<String> suggestionList) {

    }

    @Override
    public void execute(Quati quati, String argument, Map<String, String> parameters, Set<String> flags) {

    }
}