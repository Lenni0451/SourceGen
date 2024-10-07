package net.lenni0451.sourcegen.steps.impl.decompile;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;
import java.io.IOException;

public class ReconstructStep implements GeneratorStep {

    private final File input;
    private final File mappings;
    private final File output;

    public ReconstructStep(final File input, final File mappings, final File output) {
        this.input = input;
        this.mappings = mappings;
        this.output = output;
    }

    @Override
    public void printStep() {
        System.out.println("Applying mappings using Reconstruct...");
    }

    @Override
    public void run() throws IOException {
        Commands.Reconstruct.applyMappings(this.input, this.mappings, this.output);
    }

}
