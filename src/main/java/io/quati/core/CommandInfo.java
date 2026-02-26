package io.quati.core;

import io.quati.api.Action;
import io.quati.api.Argument;
import io.quati.api.Command;
import io.quati.api.Flag;
import io.quati.api.Option;
import io.quati.util.Utils;
import org.jline.reader.Candidate;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CommandInfo(
        String name,
        String description,
        Optional<ArgumentInfo> argumentOpt,
        List<OptionsSupport> optionList,
        List<OptionsSupport> flagList,
        Action action) {

    public static CommandInfo of(Class<? extends Action> commandClass) {
        if (!commandClass.isAnnotationPresent(Command.class))
            throw new RuntimeException("The @Command annotation is missing from " + commandClass);

        try {
            var command = commandClass.getAnnotation(Command.class);
            var action = commandClass.getDeclaredConstructor().newInstance();
            ArgumentInfo arg = null;
            var opts = new ArrayList<OptionsSupport>();
            var flags = new ArrayList<OptionsSupport>();
            for (var field : commandClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Argument.class))
                    arg = ArgumentInfo.of(field);
                else if (field.isAnnotationPresent(Option.class))
                    opts.add(OptionInfo.of(field));
                else if (field.isAnnotationPresent(Flag.class))
                    flags.add(FlagInfo.of(field));
            }
            return new CommandInfo(command.name(), command.desc(), Optional.ofNullable(arg), opts, flags, action);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Candidate> candidates() {
        var list = new ArrayList<Candidate>();
        optionList.stream()
                .filter(option -> option.hasRoomFor(this))
                .map(OptionInfo.class::cast)
                .map(opt -> Utils.candidate(opt.option(), opt.label(), opt.description()))
                .forEach(list::add);
        flagList.stream()
                .filter(flag -> flag.hasRoomFor(this))
                .map(FlagInfo.class::cast)
                .map(flag -> Utils.candidate(flag.option(), flag.option(), flag.description()))
                .forEach(list::add);
        return list;
    }

    public boolean hasPosition(int position) {
        return argumentOpt.isPresent()
                && argumentOpt.get().hasPosition(position);
    }

    public boolean hasFlag(String name) {
        return flagList
                .stream()
                .anyMatch(opt -> opt.is(name));
    }

    public boolean hasOption(String name) {
        return optionList
                .stream()
                .anyMatch(opt -> opt.is(name));
    }

    public OptionInfo option(String name) {
        for (var opt : optionList)
            if (opt.is(name))
                return (OptionInfo) opt;
        return null;
    }

    public String optionId(String name) {
        for (var opt : optionList)
            if (opt.is(name))
                return opt.option();
        return null;
    }

    public FlagInfo flag(String name) {
        for (var flag : flagList)
            if (flag.is(name))
                return (FlagInfo) flag;
        return null;
    }

    public void addArgument(String argument) {
        argumentOpt.ifPresent(argumentInfo ->
                setValue(action, argumentInfo.field(), argument));
    }

    public void setFlag(String name) {
        var info = flag(name);
        if (info != null)
            setValue(action, info.field(), Boolean.TRUE);
    }

    public void putOption(String name, String value) {
        var info = option(name);
        if (info != null)
            setValue(action, info.field(), value);
    }

    public boolean existsStartingWith(String partialName) {
        return optionList().stream().anyMatch(opt -> opt.startsWith(partialName))
                || flagList().stream().anyMatch(flag -> flag.startsWith(partialName));
    }

    public boolean hasRoomFor(String flagOption) {
        try {
            var flagInfo = flag(flagOption);
            if (flagInfo != null) {
                var value = flagInfo.field().get(action);
                return value instanceof Boolean bool && !bool;
            }
            var optionInfo = option(flagOption);
            if (optionInfo == null)
                return false;
            var arity = optionInfo.arity();
            var value = optionInfo.field().get(action);
            return switch (value) {
                case null -> arity.hasPosition(1);
                case String str -> str.isEmpty();
                case List<?> list -> list.size() < arity.max();
                default -> false;
            };
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validate(Quati quati) {
        try {
            if (argumentOpt.isPresent()) {
                var arg = argumentOpt.get();
                var arity = arg.arity();
                var value = arg.field().get(action);
                if (!arity.validate(value)) {
                    quati.error("The argument '%s' is invalid!%n", arg.label());
                    return false;
                }
            }
            for (var option : optionList)
                if (option instanceof OptionInfo optInfo) {
                    var arity = optInfo.arity();
                    var value = optInfo.field().get(action);
                    if (!arity.validate(value)) {
                        quati.error("The option '%s' (%s) is required!%n",
                                optInfo.option(),
                                String.join(", ", optInfo.longOptions()));
                        return false;
                    }
                }
            return true;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void setValue(Object object, Field field, Object value) {
        try {
            if (wrap(field.getType()) == wrap(value.getClass()))
                field.set(object, value);
            else if (isStringList(field) && value instanceof String string) {
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

    private static Class<?> wrap(Class<?> primitive) {
        if (primitive == boolean.class)
            return Boolean.class;
        else
            return primitive;
    }

    private static boolean isStringList(Field field) {
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