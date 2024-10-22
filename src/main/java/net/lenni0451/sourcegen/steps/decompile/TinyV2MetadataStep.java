package net.lenni0451.sourcegen.steps.decompile;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.remapping.special.TinyV2MetadataMapper;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TinyV2MetadataStep implements GeneratorStep {

    private final String message;
    private final Runner runner;

    public TinyV2MetadataStep(final Map<String, byte[]> entries, final File mappingsOrSource, final List<String> comments) {
        this.message = "Generating tiny v2 metadata...";
        this.runner = () -> TinyV2MetadataMapper.generate(entries, mappingsOrSource, comments);
    }

    public TinyV2MetadataStep(final File baseDir, final List<String> comments) {
        this.message = "Applying tiny v2 metadata...";
        this.runner = () -> TinyV2MetadataMapper.apply(baseDir, comments);
    }

    @Override
    public void printStep() {
        System.out.println(this.message);
    }

    @Override
    public void run() throws Exception {
        this.runner.run();
    }


    @FunctionalInterface
    private interface Runner {
        void run() throws Exception;
    }

}
