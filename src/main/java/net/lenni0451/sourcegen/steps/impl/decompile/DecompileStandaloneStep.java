package net.lenni0451.sourcegen.steps.impl.decompile;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;
import java.io.IOException;

public class DecompileStandaloneStep implements GeneratorStep {

    private final File input;
    private final File output;

    public DecompileStandaloneStep(final File input, final File output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void printStep() {
        System.out.println("Decompiling using VineFlower...");
    }

    @Override
    public void run() throws IOException {
        Commands.Vineflower.decompileStandalone(this.input, this.output);
    }

}
