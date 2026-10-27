package br.svc.relay;

import java.io.*;
import java.util.concurrent.TimeUnit;

/** Executa shell e captura saida, com timeout duro. */
public class Exec {
    public static String run(String cmd) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            final Process pp = p;
            StringBuilder so = new StringBuilder(), se = new StringBuilder();
            Thread t1 = new Thread(() -> slurp(pp.getInputStream(), so));
            Thread t2 = new Thread(() -> slurp(pp.getErrorStream(), se));
            t1.start(); t2.start();
            boolean ok = p.waitFor(30, TimeUnit.SECONDS);
            t1.join(2000); t2.join(2000);
            String r = so.toString();
            if (se.length() > 0) r += "\n[stderr]\n" + se;
            r += ok ? "\n[exit " + p.exitValue() + "]" : "\n[timeout 30s - processo morto]";
            return r.trim().isEmpty() ? "[sem saida]" : r;
        } catch (Exception e) {
            return "erro exec: " + e;
        } finally {
            if (p != null) p.destroy();
        }
    }
    private static void slurp(InputStream s, StringBuilder sb) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(s))) {
            String l; while ((l = r.readLine()) != null) sb.append(l).append('\n');
        } catch (Exception ignored) { }
    }
}
