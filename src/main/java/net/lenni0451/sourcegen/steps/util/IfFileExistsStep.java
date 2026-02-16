package net.lenni0451.sourcegen.steps.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class IfFileExistsStep implements GeneratorStep {

    private final File file;
    private final Function<File, GeneratorStep> step;

    @Override
    public void printStep() {
        log.info("Checking if file '{}' exist...", this.file.getName());
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
