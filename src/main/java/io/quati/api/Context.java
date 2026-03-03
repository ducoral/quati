package io.quati.api;

import io.quati.core.Quati;
import io.quati.feature.datasource.DataSourceFeature;
import io.quati.feature.driver.DriverFeature;
import io.quati.feature.schema.SchemaFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Context {

    Quati quati();

    void output(String format, Object... args);

    void error(String format, Object... args);

    void error(Exception e);

    Path repository();

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

    default void outputSuccessfully(String targetLabel, String target, String event) {
        quati().output("%s `bb`%s`:` %s `gg`successfully!`:`%n", targetLabel, target, event);
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
