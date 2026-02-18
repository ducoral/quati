package io.quati.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Quati {

    private final Map<String, Command.Info> infoMap = new HashMap<>();

    private final Map<Class<?>, Command> commandMap = new HashMap<>();

    public Quati(List<Class<? extends Command>> commandsList) {
        try {
            for (var command : commandsList) {
                var instance = command.getDeclaredConstructor().newInstance();
                infoMap.put(instance.name(), instance.info());
                commandMap.put(instance.getClass(), instance);
            }
        } catch (Exception e) {
            error("rB{%s%n}", e.getMessage());
        }
    }

    public void execute(String[] args) {
        new TabCompletion(this).execute(args);
        new ArgumentsValidation(this).execute(args);
        new CommandExecution(this).execute(args);
    }

    public List<String> commandNames() {
        return new ArrayList<>(infoMap.keySet());
    }

    public boolean contains(String commandName) {
        return infoMap.containsKey(commandName);
    }

    public boolean containsStartWith(String commandNamePart) {
        return infoMap
                .keySet()
                .stream()
                .anyMatch(command -> command.startsWith(commandNamePart));
    }

    public Command command(String commandName) {
        return contains(commandName)
                ? infoMap.get(commandName).instance()
                : null;
    }

    public boolean contains(Class<?> commandType) {
        return commandMap.containsKey(commandType);
    }

    public Command.Info info(String command) {
        return infoMap.get(command);
    }

    public <T extends Command> T command(Class<T> commandType) {
        return contains(commandType)
                ? commandType.cast(commandMap.get(commandType))
                : null;
    }

    public void output(String format, Object... args) {
        System.out.printf(ColorFilter.apply(format), args);
    }

    public void error(String format, Object... args) {
        System.err.printf(ColorFilter.apply(format), args);
    }
}
