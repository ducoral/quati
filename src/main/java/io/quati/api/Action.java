package io.quati.api;

import java.util.List;

public interface Action {

    default void completeArg(int argPos, String value, List<String> completion) {
    }

    default void completeOpt(String opt, String value, List<String> completion) {
    }

    void execute(Context ctx);
}
