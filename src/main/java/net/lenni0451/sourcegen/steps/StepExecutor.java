package net.lenni0451.sourcegen.steps;

import net.lenni0451.commons.collections.Lists;

import java.util.List;

public class StepExecutor implements GeneratorStep {

    private final List<GeneratorStep> steps;

    public StepExecutor(final GeneratorStep... steps) {
        this(Lists.arrayList(steps));
    }

    public StepExecutor(final List<GeneratorStep> steps) {
        this.steps = steps;
    }

    @Override
    public void printStep() {
    }

    @Override
    public void run() throws Exception {
        for (GeneratorStep step : this.steps) {
            step.printStep();
            step.run();
        }
    }

}
