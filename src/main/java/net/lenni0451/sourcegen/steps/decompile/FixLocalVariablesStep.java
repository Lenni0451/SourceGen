package net.lenni0451.sourcegen.steps.decompile;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.asm.LocalVariableFixer;

import java.util.Map;

public class FixLocalVariablesStep implements GeneratorStep {

    private final Map<String, byte[]> entries;

    public FixLocalVariablesStep(final Map<String, byte[]> entries) {
        this.entries = entries;
    }

    @Override
    public void printStep() {
        System.out.println("Fixing local variable names...");
    }

    @Override
    public void run() throws Exception {
        LocalVariableFixer.run(this.entries);
    }

}
