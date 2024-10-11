package net.lenni0451.sourcegen.steps.impl.io;

import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.nio.file.Files;

public class CleanRepoStep implements GeneratorStep {

    private final File repoDir;
    private final File defaultsDir;

    public CleanRepoStep(final File repoDir, final File defaultsDir) {
        this.repoDir = repoDir;
        this.defaultsDir = defaultsDir;
    }

    @Override
    public void printStep() {
        System.out.println("Cleaning repository...");
    }

    @Override
    public void run() throws Exception {
        for (File file : this.repoDir.listFiles()) {
            if (file.getName().equals(".git")) continue;
            FileUtils.recursiveDelete(file);
        }
        if (this.defaultsDir.exists()) {
            for (File file : this.defaultsDir.listFiles()) {
                Files.copy(file.toPath(), new File(this.repoDir, file.getName()).toPath());
            }
        } else {
            //Give the user a hint
            this.defaultsDir.mkdirs();
        }
    }

}
