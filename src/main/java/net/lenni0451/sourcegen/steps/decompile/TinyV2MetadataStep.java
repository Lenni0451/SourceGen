package net.lenni0451.sourcegen.steps.decompile;

import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.remapping.special.TinyV2MetadataMapper;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TinyV2MetadataStep implements GeneratorStep {

    private final Action action;
    private final Map<String, byte[]> entries;
    private final File mappingsOrSource;
    private final List<String> comments;

    public TinyV2MetadataStep(final Map<String, byte[]> entries, final File mappingsOrSource, final List<String> comments) {
        this.action = Action.GENERATE;
        this.entries = entries;
        this.mappingsOrSource = mappingsOrSource;
        this.comments = comments;
    }

    public TinyV2MetadataStep(final File baseDir, final List<String> comments) {
        this.action = Action.APPLY;
        this.entries = null;
        this.mappingsOrSource = baseDir;
        this.comments = comments;
    }

    @Override
    public void printStep() {
        switch (this.action) {
            case GENERATE -> System.out.println("Generating tiny v2 metadata...");
            case APPLY -> System.out.println("Applying tiny v2 metadata...");
            default -> throw new IllegalStateException("Unexpected value: " + this.action);
        }
    }

    @Override
    public void run() throws Exception {
        TinyV2MetadataMapper mapper = new TinyV2MetadataMapper(this.entries, this.mappingsOrSource, this.comments);
        switch (this.action) {
            case GENERATE -> mapper.generate();
            case APPLY -> mapper.apply();
        }
    }


    public enum Action {
        GENERATE, APPLY
    }

}
