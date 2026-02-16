package net.lenni0451.sourcegen.steps.decompile;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.File;

@Slf4j
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
        log.info("Decompiling {} using VineFlower...", this.input.getName());
    }

    @Override
    public void run() throws Exception {
        Commands.Vineflower.decompileWithLib(this.input, this.library, this.output);
    }

}
