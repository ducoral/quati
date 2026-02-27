package io.quati.feature.datasource;

import io.quati.api.Action;
import io.quati.api.Command;
import io.quati.api.Context;
import io.quati.core.AnsiColor;
import io.quati.core.AnsiStyle;

@Command(name = "list", description = "list datasource")
public class DataSourceList implements Action {

    @Override
    public void execute(Context ctx) {
        var str = AnsiColor.BLUE.fg("azul bold italico", AnsiStyle.BOLD_ITALIC);
        str += " normal ";
        str += AnsiColor.GREEN.fg("verde normal italico", AnsiStyle.ITALIC);
        str += " normal ";
        str += AnsiColor.RED.fg("vermelho bold", AnsiStyle.BOLD);
        str += " normal %n";
        ctx.output(str);
    }
}
