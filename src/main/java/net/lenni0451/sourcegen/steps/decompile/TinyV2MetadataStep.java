package net.lenni0451.sourcegen.steps.decompile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.lenni0451.commons.asm.mappings.meta.ClassMetaMapping;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.remapping.special.TinyV2MetadataMapper;

import java.io.File;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TinyV2MetadataStep implements GeneratorStep {

    public static TinyV2MetadataStep generate(final Map<String, byte[]> entries, final File mappings) {
        return new TinyV2MetadataStep("Generating tiny v2 metadata...", () -> TinyV2MetadataMapper.generate(entries, mappings));
    }

    public static TinyV2MetadataStep generate(final Map<String, byte[]> entries, final List<ClassMetaMapping> mappings) {
        return new TinyV2MetadataStep("Generating tiny v2 metadata...", () -> TinyV2MetadataMapper.generate(entries, mappings));
    }

    public static TinyV2MetadataStep apply(final File baseDir) {
        return new TinyV2MetadataStep("Applying tiny v2 metadata...", () -> TinyV2MetadataMapper.apply(baseDir));
    }


    private final String message;
    private final Runner runner;

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
