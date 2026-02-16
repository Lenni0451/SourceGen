package net.lenni0451.sourcegen.steps.io;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;

@Slf4j
public class CleanRepoStep implements GeneratorStep {

    private final File repoDir;

    public CleanRepoStep(final File repoDir) {
        this.repoDir = repoDir;
    }

    @Override
    public void printStep() {
        log.info("Cleaning repository...");
    }

    @Override
    public void run() throws Exception {
        for (File file : this.repoDir.listFiles()) {
            if (file.getName().equals(".git")) continue;
            FileUtils.recursiveDelete(file);
        }
    }

}
