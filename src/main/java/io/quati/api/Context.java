package io.quati.api;

public interface Context {

    void output(String format, Object... args);

    void error(String format, Object... args);
}
