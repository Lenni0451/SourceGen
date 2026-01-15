package net.lenni0451.sourcegen.utils.external;

import net.lenni0451.sourcegen.Config;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class Commands {

    private static final File CURRENT_DIR = new File(".");
    private static final File VINEFLOWER_JAR = new File(Config.External.vineflowerJar);
    private static final File HYTALE_DOWNLOADER_EXECUTABLE = new File(Config.External.hytaleDownloaderExecutable);

    public static Git git(final File repoDir) {
        return new Git(repoDir);
    }

    public static class Git {

        private final File repoDir;

        private Git(final File repoDir) {
            this.repoDir = repoDir;
        }

        public void clone(final String repoURL) throws IOException {
            Executor.execute(new File("."), Config.External.gitPath, "clone", repoURL, this.repoDir.getAbsolutePath());
        }

        public void fetchAll() throws IOException {
            Executor.execute(this.repoDir, Config.External.gitPath, "fetch", "--all");
        }

        public void resetHardHead(final String branch) throws IOException {
            Executor.execute(this.repoDir, Config.External.gitPath, "reset", "--hard", "origin/" + branch);
        }

        public boolean checkout(final String branch) throws IOException {
            return Executor.execute(this.repoDir, Collections.emptyMap(), new int[]{0, 1}, Config.External.gitPath, "checkout", branch).exitCode() == 0;
        }

        public void checkoutOrphan(final String branch) throws IOException {
            Executor.execute(this.repoDir, Config.External.gitPath, "checkout", "--orphan", branch);
        }

        public void rmAll() throws IOException {
            Executor.execute(this.repoDir, Collections.emptyMap(), new int[]{0, 128}, Config.External.gitPath, "rm", "-rf", ".");
        }

        public void setConfig(final String name, final String email) throws IOException {
            Executor.execute(this.repoDir, Config.External.gitPath, "config", "user.name", name);
            Executor.execute(this.repoDir, Config.External.gitPath, "config", "user.email", email);
        }

        public String latestCommitMessage(final String branch) throws IOException {
            String response = Executor.execute(this.repoDir, Collections.emptyMap(), new int[]{0, 128}, Config.External.gitPath, "log", "--pretty=format:\"%s\"", "-b", branch).output();
            return response.split("\n")[0].replace("\"", "");
        }

        public void addAll() throws IOException {
            Executor.execute(this.repoDir, Config.External.gitPath, "add", "--all");
        }

        public void commit(final String message, final Date commitDate) throws IOException {
            String commitDateString = String.format("%tF %<tT %<tz", commitDate);
            Map<String, String> env = Map.of(
                    "GIT_COMMITTER_DATE", commitDateString,
                    "GIT_AUTHOR_DATE", commitDateString
            );
            Executor.execute(this.repoDir, env, Config.External.gitPath, "commit", "--allow-empty", "-m", message);
        }

        public void push(final String branch) throws IOException {
            Executor.execute(this.repoDir, Config.External.gitPath, "push", "--set-upstream", "origin", branch);
        }

    }

    public static class Vineflower {

        private static final String[] BASE_COMMAND = {Config.External.javaPath, "-Xmx" + Config.External.vineflowerRam, "-jar", VINEFLOWER_JAR.getAbsolutePath()};

        public static boolean exists() {
            return VINEFLOWER_JAR.exists();
        }

        public static void decompileStandalone(final File input, final File output) throws IOException {
            String[] args = {input.getAbsolutePath(), output.getAbsolutePath()};
            Executor.execute(CURRENT_DIR, BASE_COMMAND, Config.External.vineflowerArgs, args);
        }

        public static void decompileWithLib(final File input, final File library, final File output) throws IOException {
            String[] args = {"-e=" + library.getAbsolutePath(), input.getAbsolutePath(), output.getAbsolutePath()};
            Executor.execute(CURRENT_DIR, BASE_COMMAND, Config.External.vineflowerArgs, args);
        }

    }

    public static class HytaleDownloader {

        public static boolean exists() {
            return HYTALE_DOWNLOADER_EXECUTABLE.exists();
        }

        public static String getLatestVersion() throws IOException {
            String response = Executor.execute(CURRENT_DIR, HYTALE_DOWNLOADER_EXECUTABLE.getAbsolutePath(), "-skip-update-check", "-print-version").output();
            return response.trim();
        }

        public static void downloadLatestVersion(final String fileName) throws IOException {
            Executor.execute(CURRENT_DIR, HYTALE_DOWNLOADER_EXECUTABLE.getAbsolutePath(), "-skip-update-check", "-download-path", fileName);
        }

    }

}
