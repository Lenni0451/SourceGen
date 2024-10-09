package net.lenni0451.sourcegen.steps.impl.decompile;

import net.lenni0451.commons.Sneaky;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.asm.remapping.BaseRemapper;

import java.io.IOException;

public class RemapStep implements GeneratorStep {

    private final BaseRemapper remapper;

    public RemapStep(final BaseRemapper remapper) {
        this.remapper = remapper;
    }

    @Override
    public void printStep() {
        System.out.println("Applying mappings using Reconstruct...");
    }

    @Override
    public void run() throws IOException {
        try {
            this.remapper.remap();
        } catch (Exception e) {
            Sneaky.sneak(e);
        }
    }

}
