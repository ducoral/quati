package io.quati.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Command {

    record Argument(boolean required, String label, String description) {
    }

    record Option(boolean required, String shortName, String longName, String label, String description) {
    }

    record Flag(String shortName, String longName, String description) {
    }

    record Params(List<String> args, Map<String, String> opts, Set<String> flags) {
        public static Params create() {
            return new Params(new ArrayList<>(), new HashMap<>(), new HashSet<>());
        }

        public boolean containsOptOrFlag(String name) {
            return opts.containsKey(name)
                    || flags.contains(name);
        }
    }

    record Action(String name, String description, List<Argument> args, Set<Option> opts, Set<Flag> flags) {
        public static Action of(String name, String description) {
            return new Action(name, description, new ArrayList<>(), new HashSet<>(), new HashSet<>());
        }
    }

    record Info(String name, String description, Command instance, Set<Action> actions) {
        public static Info of(String name, String description, Command instance) {
            return new Info(name, description, instance, new HashSet<>());
        }

        public List<String> actionNames() {
            return new ArrayList<>(actions
                    .stream()
                    .map(Action::name)
                    .toList());
        }

        public boolean contains(String actionName) {
            return actions
                    .stream()
                    .anyMatch(action -> action.name().equals(actionName));
        }

        public boolean containsStartWith(String actionNamePart) {
            return actions
                    .stream()
                    .anyMatch(action -> action.name().startsWith(actionNamePart));
        }
    }

    interface ActionBuilder {
        ActionBuilder argument(boolean required, String label, String description);

        ActionBuilder option(boolean required, String shortName, String longName, String label, String description);

        ActionBuilder flag(String shortName, String longName, String description);
    }

    interface Builder {
        ActionBuilder action(String name, String description);
    }

    String name();

    String description();

    void configure(Builder builder);

    void completionArgument(Quati quati, Params params, List<String> completion);

    void completionOption(Quati quati, String option, Params params, List<String> completion);

    void execute(Quati quati, Params params);

    default Info info() {
        var info = Info.of(name(), description(), this);
        configure(new Builder() {
            @Override
            public ActionBuilder action(String name, String description) {
                var action = Action.of(name, description);
                info.actions.add(action);
                return new ActionBuilder() {
                    @Override
                    public ActionBuilder argument(boolean required, String label, String description) {
                        action.args.add(new Argument(required, label, description));
                        return this;
                    }

                    @Override
                    public ActionBuilder option(boolean required, String shortName, String longName, String label, String description) {
                        action.opts.add(new Option(required, shortName, longName, label, description));
                        return this;
                    }

                    @Override
                    public ActionBuilder flag(String shortName, String longName, String description) {
                        action.flags.add(new Flag(shortName, longName, description));
                        return this;
                    }
                };
            }
        });
        return info;
    }
}