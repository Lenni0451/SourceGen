package net.lenni0451.sourcegen.steps.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.JarUtils;

import java.io.File;
import java.util.Map;

public class WriteJarEntriesStep implements GeneratorStep {

    private final Map<String, byte[]> entries;
    private final File output;

    public WriteJarEntriesStep(final Map<String, byte[]> entries, final File output) {
        this.entries = entries;
        this.output = output;
    }

    @Override
    public void printStep() {
        System.out.println("Writing jar entries to " + this.output.getName() + "...");
    }

    @Override
    public void run() throws Exception {
        JarUtils.write(this.output, this.entries);
    }

}
