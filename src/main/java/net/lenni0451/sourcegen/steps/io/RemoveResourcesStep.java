package net.lenni0451.sourcegen.steps.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class RemoveResourcesStep implements GeneratorStep {

    private final File repoDir;
    private final Predicate<File> shouldDelete;
    private final List<File> keepFiles;

    public RemoveResourcesStep(final File repoDir, final File... keepFiles) {
        this(repoDir, file -> !file.getName().toLowerCase(Locale.ROOT).endsWith(".java"), keepFiles);
    }

    public RemoveResourcesStep(final File repoDir, final Predicate<File> shouldDelete, final File... keepFiles) {
        this.repoDir = repoDir;
        this.shouldDelete = shouldDelete;
        this.keepFiles = List.of(keepFiles);
    }

    @Override
    public void printStep() {
        System.out.println("Removing resources...");
    }

    @Override
    public void run() throws Exception {
        for (File file : this.repoDir.listFiles()) {
            if (file.getName().equals(".git")) continue;
            this.checkAndRemove(file);
        }
    }

    private void checkAndRemove(final File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                this.checkAndRemove(f);
            }
            if (file.listFiles().length == 0) {
                file.delete();
            }
        } else if (file.isFile()) {
            if (!this.keepFiles.contains(file) && this.shouldDelete.test(file)) {
                file.delete();
            }
        }
    }

}
