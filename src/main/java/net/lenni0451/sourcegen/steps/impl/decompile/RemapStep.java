package net.lenni0451.sourcegen.steps.impl.decompile;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.remapping.BaseRemapper;

public class RemapStep implements GeneratorStep {

    private final BaseRemapper remapper;

    public RemapStep(final BaseRemapper remapper) {
        this.remapper = remapper;
    }

    @Override
    public void printStep() {
        System.out.println("Applying mappings...");
    }

    @Override
    public void run() throws Exception {
        this.remapper.remap();
    }

}
