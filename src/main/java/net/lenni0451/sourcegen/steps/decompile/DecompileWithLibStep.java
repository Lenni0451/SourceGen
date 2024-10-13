package net.lenni0451.sourcegen.steps.decompile;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;

public class DecompileWithLibStep implements GeneratorStep {

    private final File input;
    private final File library;
    private final File output;

    public DecompileWithLibStep(final File input, final File library, final File output) {
        this.input = input;
        this.library = library;
        this.output = output;
    }

    @Override
    public void printStep() {
        System.out.println("Decompiling " + this.input.getName() + " using VineFlower...");
    }

    @Override
    public void run() throws Exception {
        Commands.Vineflower.decompileWithLib(this.input, this.library, this.output);
    }

}
