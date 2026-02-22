package io.quati.core;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Flag;
import io.quati.api.Option;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record CommandInfo(
        String name,
        String desc,
        Optional<ArgumentInfo> args,
        Map<String, OptionInfo> opts,
        Map<String, FlagInfo> flags,
        Action action) {

    public static CommandInfo of(Class<? extends Action> commandClass) {
        if (!commandClass.isAnnotationPresent(Command.class))
            throw new RuntimeException("The @Command annotation is missing from " + commandClass);

        try {
            var command = commandClass.getAnnotation(Command.class);
            var action = commandClass.getDeclaredConstructor().newInstance();
            ArgumentInfo arg = null;
            var opts = new HashMap<String, OptionInfo>();
            var flags = new HashMap<String, FlagInfo>();
            for (var field : commandClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Argument.class))
                    arg = ArgumentInfo.of(field);
                else if (field.isAnnotationPresent(Option.class))
                    OptionInfo.of(field).put(opts);
                else if (field.isAnnotationPresent(Flag.class))
                    FlagInfo.of(field).put(flags);
            }
            return new CommandInfo(command.name(), command.desc(), Optional.ofNullable(arg), opts, flags, action);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasPosition(int position) {
        return args.isPresent() && args.get().hasPosition(position);
    }

    public boolean hasFlag(String name) {
        return flags.containsKey(name);
    }

    public boolean hasOption(String name) {
        return opts.containsKey(name);
    }

    public void addArgument(String argument) {
        args.ifPresent(argumentInfo ->
                setValue(action, argumentInfo.field(), argument));
    }

    public void setFlag(String name) {
        if (hasFlag(name))
            setValue(action, flags.get(name).field(), Boolean.TRUE);
    }

    public void putOption(String name, String value) {
        if (hasOption(name))
            setValue(action, opts.get(name).field(), value);
    }

    public boolean existsStartingWith(String partialName) {
        return opts().keySet().stream().anyMatch(opt -> opt.startsWith(partialName))
                || flags().keySet().stream().anyMatch(flag -> flag.startsWith(partialName));
    }

    public Set<String> flagsAndOptions() {
        var names = new HashSet<>(flags.keySet());
        names.addAll(opts.keySet());
        return names;
    }

    public boolean hasRoomFor(String opt) {
        if (!hasOption(opt))
            return false;
        try {
            var info = opts().get(opt);
            var arity = info.arity();
            var value = info.field().get(action);
            return switch (value) {
                case null -> arity.hasPosition(1);
                case String str -> !str.isEmpty() && arity.hasPosition(1);
                case List<?> list -> arity.hasPosition(list.size() + 1);
                default -> false;
            };
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void setValue(Object object, Field field, Object value) {
        try {
            if (field.getType() == value.getClass())
                field.set(object, value);
            else if (isListOfString(field) && value instanceof String string) {
                var list = (List<String>) field.get(object);
                if (list == null) {
                    list = new ArrayList<>();
                    field.set(object, list);
                }
                list.add(string);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isListOfString(Field field) {
        if (field != null && field.getGenericType() instanceof ParameterizedType parameterized) {
            var raw = parameterized.getRawType();
            if (raw instanceof Class<?> clazz && List.class.isAssignableFrom(clazz)) {
                var args = parameterized.getActualTypeArguments();
                if (args.length > 0 && args[0] instanceof Class<?> type)
                    return type == String.class;
            }
        }
        return false;
    }
}