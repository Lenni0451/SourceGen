package net.lenni0451.sourcegen.steps.decompile;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.remapping.special.ExceptionFiller;

import java.io.File;
import java.util.Map;

@Slf4j
public class FillExceptionsStep implements GeneratorStep {

    private final Map<String, byte[]> entries;
    private final File exceptions;

    public FillExceptionsStep(final Map<String, byte[]> entries, final File exceptions) {
        this.entries = entries;
        this.exceptions = exceptions;
    }

    @Override
    public void printStep() {
        log.info("Filling exceptions...");
    }

    @Override
    public void run() throws Exception {
        if (this.exceptions.exists()) {
            ExceptionFiller.run(this.entries, this.exceptions);
        }
    }

}
