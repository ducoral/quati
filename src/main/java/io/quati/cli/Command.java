package io.quati.cli;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Command {

    record Argument(boolean required, String label, String description) {
    }

    record Parameter(boolean required, String shortName, String longName, String label, String description) {
    }

    record Flag(String shortName, String longName, String description) {
    }

    record Action(String name, String description, List<Argument> args, Set<Parameter> opts, Set<Flag> flags) {
        public static Action of(String name, String description) {
            return new Action(name, description, new ArrayList<>(), new HashSet<>(), new HashSet<>());
        }
    }

    record Info(String name, String description, Set<Action> actions) {
        public static Info of(String name, String description) {
            return new Info(name, description, new HashSet<>());
        }
    }

    interface ActionBuilder {
        ActionBuilder argument(boolean required, String label, String description);
        ActionBuilder parameter(boolean required, String shortName, String longName, String label, String description);
        ActionBuilder flag(String shortName, String longName, String description);
    }

    interface Builder {
        ActionBuilder action(String name, String description);
    }

    String name();

    String description();

    void configure(Builder builder);

    void completion(Quati quati, String argument, String completionWord, List<String> suggestionList);

    void execute(Quati quati, String argument, Map<String, String> parameters, Set<String> flags);

    default Info info() {
        var info = Info.of(name(), description());
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
                    public ActionBuilder parameter(boolean required, String shortName, String longName, String label, String description) {
                        action.opts.add(new Parameter(required, shortName, longName, label, description));
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