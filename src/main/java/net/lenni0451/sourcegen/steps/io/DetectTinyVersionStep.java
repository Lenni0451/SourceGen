package net.lenni0451.sourcegen.steps.io;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
public class DetectTinyVersionStep implements GeneratorStep {

    private final File mappings;
    private final BiConsumer<Version, List<GeneratorStep>> stepSupplier;

    public DetectTinyVersionStep(final File mappings, final BiConsumer<Version, List<GeneratorStep>> stepSupplier) {
        this.mappings = mappings;
        this.stepSupplier = stepSupplier;
    }

    @Override
    public void printStep() {
        log.info("Detecting tiny mappings version...");
    }

    @Override
    public void run() throws Exception {
        String mappingsContent = Files.readString(this.mappings.toPath());
        List<GeneratorStep> steps = new ArrayList<>();
        if (mappingsContent.startsWith("v1\t")) {
            log.info("Mappings version is v1");
            this.stepSupplier.accept(Version.V1, steps);
        } else if (mappingsContent.startsWith("tiny\t2\t0\t")) {
            log.info("Mappings version is v2");
            this.stepSupplier.accept(Version.V2, steps);
        } else {
            throw new IllegalStateException("Unknown tiny mappings version");
        }
        StepExecutor executor = new StepExecutor(steps);
        executor.run();
    }


    public enum Version {
        V1, V2
    }

}
