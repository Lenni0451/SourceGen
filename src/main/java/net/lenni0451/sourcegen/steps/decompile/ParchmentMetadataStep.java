package net.lenni0451.sourcegen.steps.decompile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lenni0451.commons.asm.mappings.meta.ClassMetaMapping;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.remapping.special.ParchmentMetadataConverter;
import net.lenni0451.sourcegen.utils.remapping.special.TinyV2MetadataMapper;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ParchmentMetadataStep implements GeneratorStep {

    public static ParchmentMetadataStep generate(final Map<String, byte[]> entries, final File mappings) {
        return new ParchmentMetadataStep("Generating parchment metadata...", () -> {
            List<ClassMetaMapping> metadata = ParchmentMetadataConverter.toTinyV2Metadata(mappings);
            TinyV2MetadataMapper.generate(entries, metadata);
        });
    }

    public static ParchmentMetadataStep apply(final File baseDir) {
        return new ParchmentMetadataStep("Applying parchment metadata...", () -> TinyV2MetadataMapper.apply(baseDir));
    }


    private final String message;
    private final Runner runner;

    @Override
    public void printStep() {
        log.info(this.message);
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
