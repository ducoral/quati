package io.quati.api;

import org.jline.reader.Candidate;

import java.util.List;

public interface Action {

    default void completeArg(Context ctx, int argPos, String value, List<Candidate> candidates) {
    }

    default void completeOpt(Context ctx, String opt, String value, List<Candidate> candidates) {
    }

    void execute(Context ctx);
}
