package io.quati;

import io.quati.cli.Command;
import io.quati.cli.Quati;
import io.quati.cmd.DataSourceCommand;
import io.quati.cmd.DriverCommand;

import java.util.List;

public class Main {

    static final List<Class<? extends Command>> FEATURES = List.of(
            DriverCommand.class,
            DataSourceCommand.class
    );

    public static void main(String[] args) {
        new Quati(FEATURES).execute(args);
    }
}
