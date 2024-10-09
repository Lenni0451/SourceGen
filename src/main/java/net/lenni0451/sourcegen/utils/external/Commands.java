package net.lenni0451.sourcegen.utils.external;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class Commands {

    private static final File CURRENT_DIR = new File(".");
    private static final File VINEFLOWER_JAR = new File("vineflower-1.10.1.jar");

    public static Git git(final File gitDir) {
        return new Git(gitDir);
    }

    public static class Git {

        public static void setConfig(final String name, final String email) throws IOException {
            Executor.execute(CURRENT_DIR, "git", "config", "--global", "user.name", name);
            Executor.execute(CURRENT_DIR, "git", "config", "--global", "user.email", email);
        }


        private final File gitDir;

        private Git(final File gitDir) {
            this.gitDir = gitDir;
        }

        public void clone(final String repoURL) throws IOException {
            Executor.execute(new File("."), "git", "clone", repoURL, this.gitDir.getAbsolutePath());
        }

        public void fetchAll() throws IOException {
            Executor.execute(this.gitDir, "git", "fetch", "--all");
        }

        public void checkout(final String branch) throws IOException {
            Executor.execute(this.gitDir, "git", "checkout", "-b", branch);
        }

        public String latestCommitMessage(final String branch) throws IOException {
            String response = Executor.execute(this.gitDir, Collections.emptyMap(), new int[]{0, 128}, "git", "log", "--pretty=format:\"%s\"", "-b", branch);
            return response.split("\n")[0].replace("\"", "");
        }

        public void addAll() throws IOException {
            Executor.execute(this.gitDir, "git", "add", "--all");
        }

        public void commit(final String message, final Date commitDate) throws IOException {
            String commitDateString = String.format("%tF %<tT %<tz", commitDate);
            Map<String, String> env = Map.of(
                    "GIT_COMMITTER_DATE", commitDateString,
                    "GIT_AUTHOR_DATE", commitDateString
            );
            Executor.execute(this.gitDir, env, "git", "commit", "-m", message);
        }

        public void push() throws IOException {
            Executor.execute(this.gitDir, "git", "push");
        }

    }

    public static class Vineflower {

        private static final String[] BASE_COMMAND = {"java", "-jar", VINEFLOWER_JAR.getAbsolutePath()};
        private static final String[] DEFAULT_OPTIONS = {"-dgs=1", "-asc=1", "-ump=0", "-rsy=1", "-aoa=1"};

        public static void decompileStandalone(final File input, final File output) throws IOException {
            String[] args = {input.getAbsolutePath(), output.getAbsolutePath()};
            Executor.execute(CURRENT_DIR, BASE_COMMAND, DEFAULT_OPTIONS, args);
        }

        public static void decompileWithLib(final File input, final File library, final File output) throws IOException {
            String[] args = {"-e=" + library.getAbsolutePath(), input.getAbsolutePath(), output.getAbsolutePath()};
            Executor.execute(CURRENT_DIR, BASE_COMMAND, DEFAULT_OPTIONS, args);
        }

    }

}
