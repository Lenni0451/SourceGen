package net.lenni0451.sourcegen.steps.impl.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.util.List;

public class RemoveAssetsStep implements GeneratorStep {

    private final File repoDir;
    private final List<File> keepFiles;

    public RemoveAssetsStep(final File repoDir, final File... keepFiles) {
        this.repoDir = repoDir;
        this.keepFiles = List.of(keepFiles);
    }

    @Override
    public void printStep() {
        System.out.println("Removing assets...");
    }

    @Override
    public void run() throws Exception {
        for (File file : this.repoDir.listFiles()) {
            if (file.getName().equals(".git")) continue;
            if (file.getName().equals("README.md")) continue;
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
            if (!this.keepFiles.contains(file) && !file.getName().endsWith(".java")) {
                file.delete();
            }
        }
    }

}
