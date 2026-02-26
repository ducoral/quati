package io.quati.core;

import io.quati.api.Action;
import io.quati.api.Command;
import io.quati.api.Feature;
import org.jline.reader.Candidate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public record FeatureInfo(String name, String desc, Class<? extends Action>[] commandClasses) {

    public static FeatureInfo of(Class<?> featureClass) {
        if (!featureClass.isAnnotationPresent(Feature.class))
            throw new RuntimeException("The @Feature annotation is missing from " + featureClass);
        var feature = featureClass.getAnnotation(Feature.class);
        return new FeatureInfo(feature.name(), feature.desc(), feature.commands());
    }

    public Set<String> commands() {
        var names = new HashSet<String>();
        for (var command : commandClasses)
            names.add(command.getAnnotation(Command.class).name());
        return names;
    }

    public boolean exists(String command) {
        for (var commandClass : commandClasses)
            if (commandClass.getAnnotation(Command.class).name().equals(command))
                return true;
        return false;
    }

    public boolean existsStartingWith(String partialCommand) {
        for (var commandClass : commandClasses)
            if (commandClass.getAnnotation(Command.class).name().startsWith(partialCommand))
                return true;
        return false;
    }

    public CommandInfo command(String name) {
        for (var commandClass : commandClasses)
            if (commandClass.getAnnotation(Command.class).name().equals(name))
                return CommandInfo.of(commandClass);
        throw new RuntimeException("The command '%s' do not exists for feature '%s'".formatted(name, this.name));
    }

    public List<Candidate> candidates() {
        return Stream.of(commandClasses)
                .map(CommandInfo::of)
                .map(command ->
                        new Candidate(
                                command.name(),
                                command.name(),
                                null,
                                command.description(),
                                null,
                                null,
                                true))
                .toList();
    }
}