package net.lenni0451.sourcegen.steps.decompile;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.remapping.BaseRemapper;

@Slf4j
public class RemapStep implements GeneratorStep {

    private final BaseRemapper remapper;

    public RemapStep(final BaseRemapper remapper) {
        this.remapper = remapper;
    }

    @Override
    public void printStep() {
        log.info("Applying mappings...");
    }

    @Override
    public void run() throws Exception {
        this.remapper.remap();
    }

}
