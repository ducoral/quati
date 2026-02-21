package io.quati.api;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface FeatureInfo {

    interface NamesSupport {
        Set<String> names();
    }

    interface DescSupport {
        String desc();
    }

    interface LabelSupport {
        String label();
    }

    interface AritySupport {
        Arity arity();
    }

    interface FieldSupport {
        Field field();
    }

    interface PosInfo extends LabelSupport, DescSupport, AritySupport, FieldSupport {
        static PosInfo of(Field field) {
            field.setAccessible(true);
            var position = field.getAnnotation(Position.class);
            return new PosInfo() {
                @Override
                public String label() {
                    return position.label();
                }

                @Override
                public String desc() {
                    return position.desc();
                }

                @Override
                public Arity arity() {
                    return Arity.of(position.arity());
                }

                @Override
                public Field field() {
                    return field;
                }
            };
        }
    }

    interface OptInfo extends NamesSupport, LabelSupport, DescSupport, AritySupport, FieldSupport {
        static OptInfo of(Field field) {
            field.setAccessible(true);
            var option = field.getAnnotation(Option.class);
            return new OptInfo() {
                @Override
                public Set<String> names() {
                    return splitNames(option.names());
                }

                @Override
                public String label() {
                    return option.label();
                }

                @Override
                public String desc() {
                    return option.desc();
                }

                @Override
                public Arity arity() {
                    return Arity.of(option.arity());
                }

                @Override
                public Field field() {
                    return field;
                }
            };
        }
    }

    interface FlagInfo extends NamesSupport, DescSupport, FieldSupport {
        static FlagInfo of(Field field) {
            field.setAccessible(true);
            var flag = field.getAnnotation(Flag.class);
            return new FlagInfo() {
                @Override
                public Set<String> names() {
                    return splitNames(flag.names());
                }

                @Override
                public String desc() {
                    return flag.desc();
                }

                @Override
                public Field field() {
                    return field;
                }
            };
        }
    }

    interface CmdInfo extends DescSupport {
        String name();

        Optional<PosInfo> pos();

        Map<String, OptInfo> opts();

        Map<String, FlagInfo> flags();

        Command cmd();

        default boolean hasPos(int pos) {
            return pos().isPresent()
                    && pos().get().arity().hasPos(pos);
        }

        default boolean hasOptOrFlag(String name) {
            return opts().containsKey(name)
                    || flags().containsKey(name);
        }

        default boolean hasOptOrFlagStartsWith(String partial) {
            var hasOpt = opts()
                    .keySet()
                    .stream()
                    .anyMatch(opt -> opt.startsWith(partial));
            if (hasOpt)
                return true;

            return flags()
                    .keySet()
                    .stream()
                    .anyMatch(opt -> opt.startsWith(partial));
        }

        default Set<String> optAndFlagNames() {
             var names = new HashSet<>(opts().keySet());
             names.addAll(flags().keySet());
             return names;
        }

        static CmdInfo of(Class<? extends Command> clazz) {
            try {
                var command = clazz.getDeclaredConstructor().newInstance();
                PosInfo pos = null;
                var opts = new HashMap<String, OptInfo>();
                var flags = new HashMap<String, FlagInfo>();
                for (var field : clazz.getDeclaredFields())
                    if (field.isAnnotationPresent(Position.class))
                        pos = PosInfo.of(field);
                    else if (field.isAnnotationPresent(Option.class)) {
                        var info = OptInfo.of(field);
                        info.names()
                                .forEach(name -> opts.put(name, info));
                    } else if (field.isAnnotationPresent(Flag.class)) {
                        var info = FlagInfo.of(field);
                        info.names()
                                .forEach(name -> flags.put(name, info));
                    }
                var positionOpt = Optional.ofNullable(pos);
                return new CmdInfo() {
                    @Override
                    public String name() {
                        return command.name();
                    }

                    @Override
                    public String desc() {
                        return command.desc();
                    }

                    @Override
                    public Optional<PosInfo> pos() {
                        return positionOpt;
                    }

                    @Override
                    public Map<String, OptInfo> opts() {
                        return opts;
                    }

                    @Override
                    public Map<String, FlagInfo> flags() {
                        return flags;
                    }

                    @Override
                    public Command cmd() {
                        return command;
                    }
                };
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    String name();

    String desc();

    Map<String, CmdInfo> cmds();

    default boolean hasCmd(String command) {
        return cmds().containsKey(command);
    }

    default boolean hasCmdStartsWith(String partial) {
        return cmds()
                .keySet()
                .stream()
                .anyMatch(cmd -> cmd.startsWith(partial));
    }

    static FeatureInfo of(Feature feature) {
        var cmds = new HashMap<String, CmdInfo>();
        for (var cmd : feature.cmds()) {
            var info = CmdInfo.of(cmd);
            cmds.put(info.name(), info);
        }
        return new FeatureInfo() {
            @Override
            public String name() {
                return feature.name();
            }

            @Override
            public String desc() {
                return feature.desc();
            }

            @Override
            public Map<String, CmdInfo> cmds() {
                return cmds;
            }
        };
    }

    private static Set<String> splitNames(String names) {
        names = names.replaceAll("\\s+", "");
        if (names.startsWith("|"))
            names = names.substring(1);
        if (names.endsWith("|"))
            names = names.substring(0, names.length() - 1);
        return Set.of(names.strip().split("\\|"));
    }
}
