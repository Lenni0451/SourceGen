package net.lenni0451.sourcegen.steps.target;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;

import java.util.ArrayList;
import java.util.List;

public abstract class LoadContextStep<T> implements GeneratorStep {

    @Override
    public void run() throws Exception {
        T context = this.loadContext();
        List<GeneratorStep> steps = new ArrayList<>();
        this.provideSteps(steps, context);
        new StepExecutor(steps).run();
    }

    protected abstract T loadContext() throws Exception;

    protected abstract void provideSteps(final List<GeneratorStep> steps, final T context) throws Exception;

}
