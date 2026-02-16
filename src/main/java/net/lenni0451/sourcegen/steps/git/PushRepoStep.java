package net.lenni0451.sourcegen.steps.git;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;

@Slf4j
public class PushRepoStep implements GeneratorStep {

    private final File repoDir;
    private final String branch;

    public PushRepoStep(final File repoDir, final String branch) {
        this.repoDir = repoDir;
        this.branch = branch;
    }

    @Override
    public void printStep() {
        log.info("Pushing repository...");
    }

    @Override
    public void run() throws Exception {
        Commands.git(this.repoDir).push(this.branch);
    }

}
