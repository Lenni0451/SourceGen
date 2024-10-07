package net.lenni0451.sourcegen.targets;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface GeneratorTarget {

    void addSteps(final List<GeneratorStep> steps);

    default void execute() throws IOException {
        List<GeneratorStep> steps = new ArrayList<>();
        this.addSteps(steps);
        new StepExecutor(steps).run();
    }

}
