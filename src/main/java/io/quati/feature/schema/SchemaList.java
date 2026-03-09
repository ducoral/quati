package io.quati.feature.schema;

import io.quati.api.Action;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Command(name = "list", description = "list schemas")
public class SchemaList implements Action {

    @Override
    public void execute(Context ctx) {
        var schema = ctx.schema();
        ctx.output("`*b`%s  %s  %s`:`%n",
                Utils.leftJust("schema", 20),
                Utils.rightJust("size", 10),
                "updated at");
        var schemas = new ArrayList<>(schema.names());
        schemas.sort(String::compareTo);
        for (var name : schemas)
            try {
                var file = ctx.repository().resolve(name);
                var size = Utils.format(Files.size(file));
                var updateAt = Files
                        .readAttributes(file, BasicFileAttributes.class)
                        .creationTime()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                ctx.output("`*`%s`:`  %s  %s%n",
                        Utils.leftJust(name, 20),
                        Utils.rightJust(size, 10),
                        updateAt);
            } catch (IOException e) {
                ctx.error(e);
            }
    }
}