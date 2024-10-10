package net.lenni0451.sourcegen.steps.impl.io;

import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;

public class CleanRepoStep implements GeneratorStep {

    private final File repoDir;

    public CleanRepoStep(final File repoDir) {
        this.repoDir = repoDir;
    }

    @Override
    public void printStep() {
        System.out.println("Cleaning repository...");
    }

    @Override
    public void run() throws Exception {
        for (File file : this.repoDir.listFiles()) {
            if (file.getName().equals(".git")) continue;
            if (file.getName().equals("README.md")) continue;
            FileUtils.recursiveDelete(file);
        }
    }

}
