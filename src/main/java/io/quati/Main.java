package io.quati;

import io.quati.core.Command;
import io.quati.core.Quati;
import io.quati.command.DataSourceCommand;
import io.quati.command.DriverCommand;

import java.util.List;

public class Main {

    static final List<Class<? extends Command>> COMMANDS = List.of(
            DriverCommand.class,
            DataSourceCommand.class
    );

    public static void main(String[] args) {
        new Quati(COMMANDS).execute(args);
    }
}
