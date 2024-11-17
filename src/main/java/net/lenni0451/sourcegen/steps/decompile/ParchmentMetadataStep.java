package net.lenni0451.sourcegen.steps.decompile;

import net.lenni0451.commons.asm.mappings.meta.ClassMetaMapping;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.remapping.special.ParchmentMetadataConverter;
import net.lenni0451.sourcegen.utils.remapping.special.TinyV2MetadataMapper;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ParchmentMetadataStep implements GeneratorStep {

    private final String message;
    private final Runner runner;

    public ParchmentMetadataStep(final Map<String, byte[]> entries, final File mappings, final List<String[]> comments) {
        this.message = "Generating parchment metadata...";
        this.runner = () -> {
            List<ClassMetaMapping> metadata = ParchmentMetadataConverter.toTinyV2Metadata(mappings);
            TinyV2MetadataMapper.generate(entries, metadata, comments);
        };
    }

    public ParchmentMetadataStep(final File baseDir, final List<String[]> comments) {
        this.message = "Applying parchment metadata...";
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
