package net.lenni0451.sourcegen.utils.external;

import net.lenni0451.commons.arrays.ArrayUtils;
import net.lenni0451.commons.threading.ThreadUtils;

import java.io.*;
import java.util.Collections;
import java.util.Map;

public class Executor {

    private static final boolean PRINT_COMMANDS = System.getProperty("sourcegen.printCommands", "false").equalsIgnoreCase("true");
    private static final boolean PRINT_PROCESS_OUTPUT = System.getProperty("sourcegen.printProcessOutput", "false").equalsIgnoreCase("true");

    public static ProcessOutput execute(final File runDir, final String[]... cmdParts) throws IOException {
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

    public static ProcessOutput execute(final File runDir, final String... cmd) throws IOException {
        return execute(runDir, Collections.emptyMap(), cmd);
    }

    public static ProcessOutput execute(final File runDir, final Map<String, String> env, final String... cmd) throws IOException {
        return execute(runDir, env, new int[]{0}, cmd);
    }

    public static ProcessOutput execute(final File runDir, final Map<String, String> env, final int[] allowedExitCodes, final String... cmd) throws IOException {
        if (PRINT_COMMANDS) System.out.println(" > " + String.join(" ", cmd));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(runDir);
        pb.environment().putAll(env);
        Process process = pb.start();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        Thread readerThread = readStream(process.getInputStream(), process.getErrorStream(), stdout);
        while (process.isAlive()) {
            if (!ThreadUtils.sleep(200)) throw new IOException("Thread was interrupted");
        }
        if (readerThread.isAlive()) {
            //If the reader thread is still alive, give it some time to finish
            if (ThreadUtils.sleep(200)) {
                if (readerThread.isAlive()) {
                    //If the reader thread is still alive, interrupt it
                    //This sometimes happens and I don't know why
                    //Before I added this, the program would just hang here forever
                    System.out.println("Reader thread is not finished after process has exited!");
                    System.out.println("Command: " + String.join(" ", cmd));
                    System.out.println("Interrupting thread to continue...");
                    readerThread.interrupt();
                }
            } else {
                throw new IOException("Thread was interrupted");
            }
        }
        String out = stdout.toString();
        int exitCode = process.exitValue();
        if (!ArrayUtils.contains(allowedExitCodes, exitCode)) {
            System.out.println();
            System.out.println("Process exited with error code " + exitCode);
            System.out.println("Command: " + String.join(" ", cmd));
            if (!PRINT_PROCESS_OUTPUT) {
                System.out.println("Output:");
                System.out.println(out);
            }
            System.exit(exitCode);
        }
        return new ProcessOutput(exitCode, out);
    }

    private static Thread readStream(final InputStream stdin, final InputStream stderr, final OutputStream os) {
        Thread reader = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = stdin.read(buffer)) != -1) {
                    os.write(buffer, 0, length);
                    if (PRINT_PROCESS_OUTPUT) System.out.write(buffer, 0, length);
                }
                while ((length = stderr.read(buffer)) != -1) {
                    os.write(buffer, 0, length);
                    if (PRINT_PROCESS_OUTPUT) System.err.write(buffer, 0, length);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(-1);
            }
        });
        reader.setDaemon(true);
        reader.start();
        return reader;
    }


    public record ProcessOutput(int exitCode, String output) {
    }

}
