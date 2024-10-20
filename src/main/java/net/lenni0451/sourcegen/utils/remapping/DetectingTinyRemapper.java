package net.lenni0451.sourcegen.utils.remapping;

import net.lenni0451.classtransform.mappings.AMapper;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

public class DetectingTinyRemapper extends BaseRemapper {

    private final File input;
    private final File output;

    public DetectingTinyRemapper(File input, File mappings, File output) {
        super(input, mappings, output);
        this.input = input;
        this.output = output;
    }

    @Override
    protected AMapper loadMapper(Map<String, byte[]> entries, File mappings) throws Exception {
        BaseRemapper delegate;
        String content = Files.readString(mappings.toPath());
        if (content.startsWith("v1")) {
            delegate = new TinyV1Remapper(this.input, mappings, this.output);
        } else if (content.startsWith("tiny\t2")) {
            delegate = new TinyV2Remapper(this.input, mappings, this.output);
        } else {
            throw new IllegalArgumentException("Unknown mappings format");
        }
        return delegate.loadMapper(entries, mappings);
    }

}
