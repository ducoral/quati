package io.quati.api;

import java.util.List;

public interface Action {

    default void completeArg(Context ctx, int argPos, String value, List<String> completion) {
    }

    default void completeOpt(Context ctx, String opt, String value, List<String> completion) {
    }

    void execute(Context ctx);
}
