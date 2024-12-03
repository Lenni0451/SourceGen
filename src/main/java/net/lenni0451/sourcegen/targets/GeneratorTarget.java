package net.lenni0451.sourcegen.targets;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;

import java.util.ArrayList;
import java.util.List;

public abstract class GeneratorTarget {

    private final String name;

    public GeneratorTarget(final String name) {
        this.name = name;
    }

    public final String getName() {
        return this.name;
    }

    public abstract void addSteps(final List<GeneratorStep> steps);

    public final void execute() throws Exception {
        List<GeneratorStep> steps = new ArrayList<>();
        this.addSteps(steps);
        new StepExecutor(steps).run();
    }

}
