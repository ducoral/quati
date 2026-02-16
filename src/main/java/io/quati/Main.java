package io.quati;

import io.quati.util.Args;

public class Main {

    public static String COMPLETION_PREFIX = "\\d+:\\w+.+";

    public static void main(String[] args) {
        try {
            if (isCompletion(args)) {
                doCompletion(args);
                System.exit(0);
            }

            var qargs = Args.parse(args);
            System.out.println(qargs.cmds);
            System.out.println(qargs.opts);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static boolean isCompletion(String[] args) {
        return args.length > 0
                && args[0] != null
                && args[0].matches(COMPLETION_PREFIX);
    }

    private static void doCompletion(String[] args) {
        var a = "";
        for (var s : args)
            a = a + '_' + s;
        System.out.println(a.replace(' ', '_'));
        System.exit(0);

        System.out.println("goiaba laranja maçã mamão limão lima abacate abacaxi");
    }
}
