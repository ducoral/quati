package io.quati.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface Context {

    void output(String format, Object... args);

    void error(String format, Object... args);

    Path repository();

    default File file(String name) {
        return repository().resolve(name).toFile();
    }

    default void files(Consumer<Path> pathConsumer) {
        try (var stream = Files.list(repository())) {
            stream.forEach(pathConsumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
