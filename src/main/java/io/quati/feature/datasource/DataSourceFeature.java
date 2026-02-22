package io.quati.feature.datasource;

import io.quati.api.Feature;

@Feature(
        name = "datasource",
        desc = "JDBC connection configuration",
        commands = {
                DataSourceList.class,
                DataSourceCreate.class,
                DataSourceTest.class,
                DataSourceInfo.class,
                DataSourceRemove.class
        })
public class DataSourceFeature {
}
