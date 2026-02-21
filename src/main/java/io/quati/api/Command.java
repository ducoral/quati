package io.quati.api;

public interface Command {

    String name();

    String desc();

    void exec(Context ctx);
}
