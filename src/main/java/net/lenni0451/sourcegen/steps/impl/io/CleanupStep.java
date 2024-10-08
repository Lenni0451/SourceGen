package net.lenni0451.sourcegen.steps.impl.io;

import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;

public class CleanupStep implements GeneratorStep {

    private final File[] files;

    public CleanupStep(final File... files) {
        this.files = files;
    }

    @Override
    public void printStep() {
        System.out.println("Cleaning up...");
    }

    @Override
    public void run() {
        for (File file : this.files) {
            FileUtils.recursiveDelete(file);
        }
    }

}
