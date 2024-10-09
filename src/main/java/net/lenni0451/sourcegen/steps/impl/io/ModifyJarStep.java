package net.lenni0451.sourcegen.steps.impl.io;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.JarUtils;

import java.io.File;
import java.util.Map;
import java.util.function.Predicate;

public class ModifyJarStep implements GeneratorStep {

    private final File input;
    private final File output;
    private final Predicate<String> filter;

    public ModifyJarStep(final File input, final File output, final Predicate<String> removeEntry) {
        this.input = input;
        this.output = output;
        this.filter = removeEntry;
    }

    @Override
    public void printStep() {
        System.out.println("Modifying jar file " + this.output.getName() + "...");
    }

    @Override
    public void run() throws Exception {
        Map<String, byte[]> entries = JarUtils.read(this.input);
        entries.entrySet().removeIf(entry -> this.filter.test(entry.getKey()));
        JarUtils.write(this.output, entries);
    }

}
