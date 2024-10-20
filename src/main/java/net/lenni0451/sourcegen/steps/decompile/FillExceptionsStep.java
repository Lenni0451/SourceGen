package net.lenni0451.sourcegen.steps.decompile;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.remapping.special.ExceptionFiller;

import java.io.File;

public class FillExceptionsStep implements GeneratorStep {

    private final File input;
    private final File exceptions;
    private final File output;

    public FillExceptionsStep(final File input, final File exceptions, final File output) {
        this.input = input;
        this.exceptions = exceptions;
        this.output = output;
    }

    @Override
    public void printStep() {
        System.out.println("Filling exceptions...");
    }

    @Override
    public void run() throws Exception {
        new ExceptionFiller(this.input, this.exceptions, this.output);
    }

}
