package io.quati.api;

import java.util.List;

public interface Command {

    String name();

    String desc();

    void tabComp(int pos, String value, List<String> compList);

    void tabComp(String opt, String value, List<String> compList);

    void exec(Context ctx);
}
