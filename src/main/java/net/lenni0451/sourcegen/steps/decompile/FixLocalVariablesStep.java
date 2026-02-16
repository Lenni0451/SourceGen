package net.lenni0451.sourcegen.steps.decompile;

import lombok.extern.slf4j.Slf4j;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.asm.LocalVariableFixer;

import java.util.Map;

@Slf4j
public class FixLocalVariablesStep implements GeneratorStep {

    private final Map<String, byte[]> entries;

    public FixLocalVariablesStep(final Map<String, byte[]> entries) {
        this.entries = entries;
    }

    @Override
    public void printStep() {
        log.info("Fixing local variable names...");
    }

    @Override
    public void run() throws Exception {
        LocalVariableFixer.run(this.entries);
    }

}
