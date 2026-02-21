package io.quati.feature.datasource;

import io.quati.api.Command;
import io.quati.api.Feature;

import java.util.List;

public class DataSourceFeature implements Feature {

    @Override
    public String name() {
        return "datasource";
    }

    @Override
    public String desc() {
        return "JDBC connection configuration";
    }

    @Override
    public List<Class<? extends Command>> cmds() {
        return List.of(
                DataSourceList.class,
                DataSourceCreate.class,
                DataSourceTest.class,
                DataSourceInfo.class,
                DataSourceRemove.class
        );
    }
}
