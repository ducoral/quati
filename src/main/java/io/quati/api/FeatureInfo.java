package io.quati.api;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

        Command obj();

        default boolean hasPos(int pos) {
            return pos().isPresent()
                    && pos().get().arity().hasPos(pos);
        }

        default boolean hasOpt(String name) {
            return opts().containsKey(name);
        }

        default boolean hasFlag(String name) {
            return flags().containsKey(name);
        }

        default boolean hasOptFlagStartsWith(String partial) {
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

        default Set<String> optFlagNames() {
            var names = new HashSet<>(opts().keySet());
            names.addAll(flags().keySet());
            return names;
        }

        @SuppressWarnings("unchecked")
        default void addPosValue(String position) {
            if (pos().isEmpty())
                return;
            try {
                var info = pos().get();
                var field = info.field();
                if (field.getType() == String.class)
                    field.set(obj(), position);
                else if (isListOfString(field)) {
                    List<String> list = (List<String>) field.get(obj());
                    if (list == null)
                        list = new ArrayList<>();
                    list.add(position);
                    field.set(obj(), list);
                } else
                    throw new RuntimeException("Invalid Java field type for Position argument");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        default void setFlag(String name) {
            if (!hasFlag(name))
                return;
            try {
                var info = flags().get(name);
                var field = info.field();
                if (field.getType() != Boolean.class)
                    throw new RuntimeException("Invalid Java field type for Flag '%s'".formatted(name));
                field.set(obj(), Boolean.TRUE);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        default void putOpt(String name, String value) {
            if (!hasOpt(name))
                return;
            try {
                var info = opts().get(name);
                var field = info.field();
                if (field.getType() == String.class)
                    field.set(obj(), value);
                else if (isListOfString(field)) {
                    List<String> list = (List<String>) field.get(obj());
                    if (list == null)
                        list = new ArrayList<>();
                    list.add(value);
                    field.set(obj(), list);
                } else
                    throw new RuntimeException("Invalid Java field type for Option '%s'".formatted(name));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        default boolean hasRoomFor(String opt) {
            if (!hasOpt(opt))
                return false;
            try {
                var info = opts().get(opt);
                var arity = info.arity();
                var value = info.field().get(obj());
                return switch (value) {
                    case null -> arity.hasPos(1);
                    case String str -> !str.isEmpty() && arity.hasPos(1);
                    case List<?> list -> arity.hasPos(list.size() + 1);
                    default -> false;
                };
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        default boolean isArityOk(String opt) {
            if (!hasOpt(opt))
                return false;
            try {
                var info = opts().get(opt);
                var arity = info.arity();
                var value = info.field().get(obj());
                return switch (value) {
                    case null -> arity.min() == 0;
                    case String str -> !str.isEmpty() || arity.min() == 0;
                    case List<?> list -> arity.min() <= (list.size() + 1)
                            && (list.size() + 1) <= arity.max();
                    default -> true;
                };
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
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
                    public Command obj() {
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

    private static Set<String> splitNames(String names) {
        names = names.replaceAll("\\s+", "");
        if (names.startsWith("|"))
            names = names.substring(1);
        if (names.endsWith("|"))
            names = names.substring(0, names.length() - 1);
        return Set.of(names.strip().split("\\|"));
    }
}
