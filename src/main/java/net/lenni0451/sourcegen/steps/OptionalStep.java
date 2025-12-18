package net.lenni0451.sourcegen.steps;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OptionalStep implements GeneratorStep {

    private final GeneratorStep step;

    @Override
    public void printStep() {
        this.step.printStep();
    }

    @Override
    public void run() throws Exception {
        try {
            this.step.run();
        } catch (Exception e) {
            System.out.println("Optional step failed, skipping...");
        }
    }

}
