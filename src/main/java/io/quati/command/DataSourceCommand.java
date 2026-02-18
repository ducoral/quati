package io.quati.command;

import io.quati.core.Command;
import io.quati.core.Quati;

import java.util.List;

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
                .option(true, "-d", "--driver", "DRIVER", "name of the installed driver")
                .option(true, "-h", "--host", "HOST", "database host")
                .option(false, "-p", "--port", "PORT", "database port")
                .option(true, "-db", "--database", "DATABASE", "database name")
                .option(true, "-u", "--user-name", "USER", "database user name")
                .option(true, "-s", "--secret", "PASSWORD", "database user password");
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
    public void completionArgument(Quati quati, Params params, List<String> completion) {

    }

    @Override
    public void completionOption(Quati quati, String option, Params params, List<String> completion) {

    }

    @Override
    public void execute(Quati quati, Params params) {

    }
}