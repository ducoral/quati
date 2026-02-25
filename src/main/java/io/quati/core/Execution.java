package io.quati.core;

import io.quati.util.Strs;

public record Execution(Quati quati) {

    public void execute(String[] args) {
        if (args.length == 0)
            quati.printUsage();
        else if (quati.exists(args[0]))
            executeFeature(quati.feature(args[0]), Strs.tail(args));
        else
            quati.error("The feature '%s' do not exists%n", args[0]);
        System.exit(1);
    }

    private void executeFeature(FeatureInfo feature, String[] args) {
        if (args.length == 0)
            quati.printUsage(feature.name());
        else if (feature.exists(args[0]))
            executeCommand(feature, args[0], Strs.tail(args));
        else
            quati.error("The command '%s' do not exists for the feature '%s'%n", args[0], feature.name());
    }

    private void executeCommand(FeatureInfo feature, String commandName, String[] args) {
        var command = feature.command(commandName);
        parseCommand(command, 1, args);
        command.validate(quati);
        command
                .action()
                .execute(new QuatiContext(quati, feature));
    }

    private void parseCommand(CommandInfo command, int pos, String[] args) {
        if (args.length == 0)
            return;
        if (args[0].startsWith("-"))
            parseOption(command, pos, args[0], Strs.tail(args));
        else if (command.hasPosition(pos)) {
            command.addArgument(args[0]);
            parseCommand(command, pos + 1, Strs.tail(args));
        } else
            quati.errorAndExit("The argument '%s' is invalid at position '%s'%n", args[0], pos);
    }

    private void parseOption(CommandInfo command, int pos, String option, String[] args) {
        if (command.hasFlag(option)) {
            command.setFlag(option);
            if (args.length > 0)
                parseCommand(command, pos, Strs.tail(args));
        } else if (command.hasOption(option)) {
            if (args.length == 0 || args[0].startsWith("-"))
                quati.errorAndExit("Missing value of option '%s'%n", option);
            else if (command.hasRoomFor(option)) {
                command.putOption(option, args[0]);
                args = Strs.tail(args);
                if (args.length == 0)
                    return;
                if (args[0].startsWith("-"))
                    parseOption(command, pos, args[0], Strs.tail(args));
                else if (command.hasRoomFor(option))
                    parseOption(command, pos, option, args);
                else
                    parseCommand(command, pos, args);
            } else
                quati.errorAndExit("The value '%s' is invalid for option '%s'%n", args[0], option);
        } else
            quati.errorAndExit("Invalid option '%s'%n", option);
    }
}
