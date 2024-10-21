package net.lenni0451.sourcegen.steps.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.JarUtils;

import java.io.File;
import java.util.Map;

public class ReadJarEntriesStep implements GeneratorStep {

    private final File input;
    private final Map<String, byte[]> entries;

    public ReadJarEntriesStep(final File input, final Map<String, byte[]> entries) {
        this.input = input;
        this.entries = entries;
    }

    @Override
    public void printStep() {
        System.out.println("Reading jar entries from " + this.input.getName() + "...");
    }

    @Override
    public void run() throws Exception {
        Map<String, byte[]> loaded = JarUtils.read(this.input);
        this.entries.clear();
        this.entries.putAll(loaded);
    }

}
