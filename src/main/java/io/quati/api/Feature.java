package io.quati.api;

import java.util.List;

public interface Feature {

    String name();

    String desc();

    List<Class<? extends Command>> cmds();

    default FeatureInfo info() {
        return FeatureInfo.of(this);
    }
}
