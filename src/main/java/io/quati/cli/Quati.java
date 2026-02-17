package io.quati.cli;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Quati {

    private final Map<Class<? extends Command>, Command> commandsMap = new HashMap<>();

    public Quati(List<Class<? extends Command>> commandsList) {
        commandsList.forEach(feature -> {
            try {
                commandsMap.put(feature, feature.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                System.err.printf("%s: %s%n", e.getClass(), e.getMessage());
            }
        });
    }

    public void execute(String[] args) {
        new TabCompletion(this).execute(args);
        new ArgumentsValidation(this).execute(args);
        new CommandExecution(this).execute(args);
    }

    public void output(String format, Object... args) {
        System.out.printf(ColorFilter.apply(format), args);
    }

    public void error(String format, Object... args) {
        System.err.printf(ColorFilter.apply(format), args);
    }

    public Map<String, Command.Info> infoMap() {
        var map = new HashMap<String, Command.Info>();
        for (var cmd : commandsMap.values())
            map.put(cmd.name(), cmd.info());
        return map;
    }
}
