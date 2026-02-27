package io.quati.feature.datasource;

import io.quati.api.Feature;
import io.quati.core.AbstractFeature;

@Feature(
        name = "datasource",
        description = "JDBC connection configuration",
        commands = {
                DataSourceList.class,
                DataSourceCreate.class,
                DataSourceTest.class,
                DataSourceInfo.class,
                DataSourceRemove.class
        })
public class DataSourceFeature extends AbstractFeature {
}
