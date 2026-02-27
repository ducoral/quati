package io.quati.core;

import io.quati.util.Utils;

public record Execution(Quati quati) {

    public void execute(String[] args) {
        if (args.length == 0)
            quati.printUsage();
        else if (quati.exists(args[0]))
            executeFeature(quati.featureInfo(args[0]), Utils.tail(args));
        else
            quati.error("The feature '%s' do not exists%n", args[0]);
    }

    private void executeFeature(FeatureInfo feature, String[] args) {
        if (args.length == 0)
            quati.printUsage(feature.name());
        else if (feature.exists(args[0]))
            executeCommand(feature, args[0], Utils.tail(args));
        else
            quati.error("The command '%s' do not exists for the feature '%s'%n", args[0], feature.name());
    }

    private void executeCommand(FeatureInfo feature, String commandName, String[] args) {
        var command = feature.commandInfo(commandName);

        if (parseCommand(command, 1, args) && command.validate(quati))
            command
                    .action()
                    .execute(new QuatiContext(quati, feature));
    }

    private boolean parseCommand(CommandInfo command, int pos, String[] args) {
        if (args.length == 0)
            return true;
        if (args[0].startsWith("-"))
            return parseOption(command, pos, args[0], Utils.tail(args));
        else if (command.hasPosition(pos)) {
            command.addArgument(args[0]);
            return parseCommand(command, pos + 1, Utils.tail(args));
        } else {
            quati.error("The argument '%s' is invalid at position '%s'%n", args[0], pos);
            return false;
        }
    }

    private boolean parseOption(CommandInfo command, int pos, String option, String[] args) {
        if (command.hasFlag(option)) {
            command.setFlag(option);
            if (args.length > 0)
                return parseCommand(command, pos, Utils.tail(args));
            return true;
        } else if (command.hasOption(option)) {
            if (args.length == 0 || args[0].startsWith("-")) {
                quati.error("Missing value of option '%s'%n", option);
                return false;
            } else if (command.hasRoomFor(option)) {
                command.putOption(option, args[0]);
                args = Utils.tail(args);
                if (args.length == 0)
                    return true;
                if (args[0].startsWith("-"))
                    return parseOption(command, pos, args[0], Utils.tail(args));
                else if (command.hasRoomFor(option))
                    return parseOption(command, pos, option, args);
                else
                    return parseCommand(command, pos, args);
            } else {
                quati.error("The value '%s' is invalid for option '%s'%n", args[0], option);
                return false;
            }
        } else {
            quati.error("Invalid option '%s'%n", option);
            return false;
        }
    }
}
