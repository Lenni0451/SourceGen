package net.lenni0451.sourcegen.utils.external;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.commons.arrays.ArrayUtils;
import net.lenni0451.commons.threading.ThreadUtils;

import java.io.*;
import java.util.Collections;
import java.util.Map;

@Slf4j
public class Executor {

    private static final int[] DEFAULT_ALLOWED_EXIT_CODES = {0};

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
        return execute(runDir, env, DEFAULT_ALLOWED_EXIT_CODES, cmd);
    }

    public static ProcessOutput execute(final File runDir, final Map<String, String> env, final int[] allowedExitCodes, final String... cmd) throws IOException {
        log.debug(" > {}", String.join(" ", cmd));
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
                    log.warn("Reader thread is not finished after process has exited!");
                    log.warn("Command: {}", String.join(" ", cmd));
                    log.warn("Interrupting thread to continue...");
                    readerThread.interrupt();
                }
            } else {
                throw new IOException("Thread was interrupted");
            }
        }
        String out = stdout.toString();
        int exitCode = process.exitValue();
        if (!ArrayUtils.contains(allowedExitCodes, exitCode)) {
            log.error("Process exited with error code {}", exitCode);
            log.info("Command: {}", String.join(" ", cmd));
            log.info("Output:");
            log.info(out);
            System.exit(exitCode);
        }
        return new ProcessOutput(exitCode, out);
    }

    private static Thread readStream(final InputStream stdin, final InputStream stderr, final OutputStream os) {
        DebugLogCollector stdoutCollector = new DebugLogCollector();
        DebugLogCollector stderrCollector = new DebugLogCollector();
        Thread reader = new Thread(() -> {
            try {
                try {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = stdin.read(buffer)) != -1) {
                        os.write(buffer, 0, length);
                        stdoutCollector.append(buffer, length);
                    }
                    while ((length = stderr.read(buffer)) != -1) {
                        os.write(buffer, 0, length);
                        stderrCollector.append(buffer, length);
                    }
                } finally {
                    stdoutCollector.flush();
                    stderrCollector.flush();
                }
            } catch (Throwable t) {
                log.error("Error while reading process output", t);
                System.exit(-1);
            }
        }, "ProcessOutputReader");
        reader.setDaemon(true);
        reader.start();
        return reader;
    }


    public record ProcessOutput(int exitCode, String output) {
    }

    private static class DebugLogCollector {
        private final StringBuilder currentLine = new StringBuilder();

        public void append(final byte[] buffer, final int length) {
            this.currentLine.append(new String(buffer, 0, length));
            int index;
            while ((index = this.currentLine.indexOf("\n")) != -1) {
                String line = this.currentLine.substring(0, index);
                log.debug(line);
                this.currentLine.delete(0, index + 1);
            }
        }

        public void flush() {
            if (!this.currentLine.isEmpty()) {
                log.debug(this.currentLine.toString());
                this.currentLine.setLength(0);
            }
        }
    }

}
