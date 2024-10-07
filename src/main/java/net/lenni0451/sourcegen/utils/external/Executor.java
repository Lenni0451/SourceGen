package net.lenni0451.sourcegen.utils.external;

import net.lenni0451.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class Executor {

    public static String execute(final File runDir, final String[]... cmdParts) throws IOException {
        int length = 0;
        for (String[] cmdPart : cmdParts) length += cmdPart.length;
        String[] cmd = new String[length];
        int index = 0;
        for (String[] cmdPart : cmdParts) {
            System.arraycopy(cmdPart, 0, cmd, index, cmdPart.length);
            index += cmdPart.length;
        }
        return execute(runDir, cmd);
    }

    public static String execute(final File runDir, final String... cmd) throws IOException {
        return execute(runDir, Collections.emptyMap(), cmd);
    }

    public static String execute(final File runDir, final Map<String, String> env, final String... cmd) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(runDir);
        pb.environment().putAll(env);
        Process process = pb.start();
        byte[] stdout = IOUtils.readAll(process.getInputStream());
        String out = new String(stdout);
        if (process.exitValue() != 0) {
            System.out.println();
            System.out.println("Process exited with error code " + process.exitValue());
            System.out.println("Command: " + String.join(" ", cmd));
            System.out.println("Output:");
            System.out.println(out);
            System.exit(process.exitValue());
        }
        return out;
    }

}
