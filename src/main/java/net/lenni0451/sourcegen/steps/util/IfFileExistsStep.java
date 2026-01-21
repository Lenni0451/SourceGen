package net.lenni0451.sourcegen.steps.util;

import lombok.RequiredArgsConstructor;
import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.util.function.Function;

@RequiredArgsConstructor
public class IfFileExistsStep implements GeneratorStep {

    private final File file;
    private final Function<File, GeneratorStep> step;

    @Override
    public void printStep() {
        System.out.println("Checking if file '" + this.file.getName() + "' exist...");
    }

    @Override
    public void run() throws Exception {
        if (this.file.exists()) {
            GeneratorStep step = this.step.apply(this.file);
            step.printStep();
            step.run();
        }
    }

}
