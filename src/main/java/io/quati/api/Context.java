package io.quati.api;

import io.quati.core.Quati;
import io.quati.feature.datasource.DataSourceFeature;
import io.quati.feature.driver.DriverFeature;
import io.quati.feature.schema.SchemaFeature;
import io.quati.util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Context {

    Map<String, Instant> TARGETS = new HashMap<>();

    Quati quati();

    void output(String format, Object... args);

    void error(String format, Object... args);

    void error(Exception e);

    default void errorNotExists(String label, String name) {
        error("%s `r`%s`:` do not exists!%n", label, name);
    }

    default void lineBreak() {
        output("%n");
    }

    Path repository();

    int width();

    int height();

    default Path file(String name) {
        return repository().resolve(name);
    }

    default void forEachFile(Consumer<Path> pathConsumer) {
        try (var stream = Files.list(repository())) {
            stream.forEach(pathConsumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default List<String> fileNames() {
        return fileNames(null);
    }

    default List<String> fileNames(Function<String, String> mapper) {
        try (var stream = Files.list(repository())) {
            Function<String, String> mapFunc = mapper == null
                    ? str -> str
                    : mapper;
            return stream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(mapFunc)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default void writeTextFile(String name, String content) {
        try {
            Files.writeString(
                    repository().resolve(name),
                    content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default String readTextFile(String name) {
        try {
            return Files.readString(repository().resolve(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default void deleteFile(String name) {
        try {
            Files.delete(repository().resolve(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default void startTarget(String target) {
        TARGETS.put(target, Instant.now());
    }

    default void endTargetSuccessfully(String label, String target, String event) {
        var start = TARGETS.get(target);
        var duration = start != null
                ? " `bb`%s`:`".formatted(Utils.readableDuration(start, Instant.now()))
                : "";
        quati().output("%s `bb`%s`:` %s `gg`successfully!`:`%s%n", label, target, event, duration);
    }

    default DriverFeature driver() {
        return quati().feature(DriverFeature.class);
    }

    default DataSourceFeature datasource() {
        return quati().feature(DataSourceFeature.class);
    }

    default SchemaFeature schema() {
        return quati().feature(SchemaFeature.class);
    }
}
