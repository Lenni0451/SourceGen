package net.lenni0451.sourcegen.steps.io;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;

@Slf4j
public class CleanupStep implements GeneratorStep {

    private final File[] files;

    public CleanupStep(final File... files) {
        this.files = files;
    }

    @Override
    public void printStep() {
        log.info("Cleaning up...");
    }

    @Override
    public void run() {
        for (File file : this.files) {
            FileUtils.recursiveDelete(file);
        }
    }

}
