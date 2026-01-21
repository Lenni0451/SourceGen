package net.lenni0451.sourcegen.targets;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;

import java.util.ArrayList;
import java.util.List;

public abstract class GeneratorTarget {

    private final String name;
    private final Requirements[] requirements;

    public GeneratorTarget(final String name, final Requirements... requirements) {
        this.name = name;
        this.requirements = requirements;
    }

    public final String getName() {
        return this.name;
    }

    public final Requirements[] getRequirements() {
        return this.requirements;
    }

    protected abstract void addSteps(final List<GeneratorStep> steps);

    /**
     * Provide a step that should be executed in a shutdown hook if the generator had an error.<br>
     * It is recommended to use this method to push already done work to prevent data loss.<br>
     * This step will not be executed if the generator finished successfully.
     *
     * @return The error step
     */
    protected GeneratorStep getErrorStep() {
        return null;
    }

    public final void execute() throws Exception {
        List<GeneratorStep> steps = new ArrayList<>();
        this.addSteps(steps);

        ErrorStepExecutor errorStepExecutor = new ErrorStepExecutor();
        Runtime.getRuntime().addShutdownHook(errorStepExecutor);
        new StepExecutor(steps).run(); //Execute all steps
        if (!Runtime.getRuntime().removeShutdownHook(errorStepExecutor)) { //If execution reaches this point, the generator finished successfully
            System.out.println("Failed to remove error step executor from shutdown hook!");
        }
    }


    private class ErrorStepExecutor extends Thread {
        @Override
        public void run() {
            GeneratorStep errorStep = GeneratorTarget.this.getErrorStep();
            if (errorStep == null) return;

            System.out.println("The generator exited abnormally, executing error step...");
            errorStep.printStep();
            try {
                errorStep.run();
            } catch (Throwable t) {
                System.out.println("Error while executing error step");
                t.printStackTrace();
            }
        }
    }

}
