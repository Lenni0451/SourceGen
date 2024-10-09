package net.lenni0451.sourcegen.steps.impl.decompile;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.asm.LocalVariableFixer;

import java.io.File;

public class FixLocalVariablesStep implements GeneratorStep {

    private final File input;
    private final File output;

    public FixLocalVariablesStep(final File input, final File output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void printStep() {
        System.out.println("Fixing local variable names...");
    }

    @Override
    public void run() throws Exception {
        LocalVariableFixer.run(this.input, this.output);
    }

}
