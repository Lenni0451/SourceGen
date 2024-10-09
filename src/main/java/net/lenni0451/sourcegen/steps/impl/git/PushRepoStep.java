package net.lenni0451.sourcegen.steps.impl.git;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;

public class PushRepoStep implements GeneratorStep {

    private final File repoDir;

    public PushRepoStep(final File repoDir) {
        this.repoDir = repoDir;
    }

    @Override
    public void printStep() {
        System.out.println("Pushing repository...");
    }

    @Override
    public void run() throws Exception {
        Commands.git(this.repoDir).push();
    }

}
