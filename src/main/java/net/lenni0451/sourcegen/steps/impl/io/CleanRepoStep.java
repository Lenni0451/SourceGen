package net.lenni0451.sourcegen.steps.impl.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.io.IOException;

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
    public void run() throws IOException {
        for (File file : this.repoDir.listFiles()) {
            if (file.getName().equals(".git")) continue;
            file.delete();
        }
    }

}
