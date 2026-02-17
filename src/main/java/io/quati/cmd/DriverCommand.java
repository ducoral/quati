package io.quati.cmd;

import io.quati.cli.Command;
import io.quati.cli.Quati;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DriverCommand implements Command {

    @Override
    public String name() {
        return "driver";
    }

    @Override
    public String description() {
        return "JDBC driver manager";
    }

    @Override
    public void configure(Builder builder) {
        builder.action("list", "list JDBC drivers")
                .flag("-a", "--all", "list all JDBC drivers")
                .flag("-i", "--installed", "list only installed JDBC drivers");

        builder.action("install", "install JDBC driver")
                .argument(true, "DRIVER", "name of driver to be installed");

        builder.action("remove", "remove JDBC driver")
                .argument(true, "DRIVER", "name of driver to be removed");
    }

    @Override
    public void completion(Quati quati, String argument, String completionWord, List<String> suggestionList) {

    }

    @Override
    public void execute(Quati quati, String argument, Map<String, String> parameters, Set<String> flags) {

    }
}
