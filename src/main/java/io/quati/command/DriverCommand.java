package io.quati.command;

import io.quati.core.Command;
import io.quati.core.Quati;

import java.util.List;

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
    public void completionArgument(Quati quati, Params params, List<String> completion) {

    }

    @Override
    public void completionOption(Quati quati, String option, Params params, List<String> completion) {

    }

    @Override
    public void execute(Quati quati, Params params) {

    }
}
