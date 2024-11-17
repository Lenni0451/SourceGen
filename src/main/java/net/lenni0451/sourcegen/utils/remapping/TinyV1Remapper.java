package net.lenni0451.sourcegen.utils.remapping;

import net.lenni0451.commons.asm.mappings.loader.MappingsLoader;
import net.lenni0451.commons.asm.mappings.loader.TinyV1MappingsLoader;

import java.io.File;
import java.util.Map;

public class TinyV1Remapper extends BaseRemapper {

    public TinyV1Remapper(final Map<String, byte[]> entries, final File mappings) {
        super(entries, mappings);
    }

    @Override
    protected MappingsLoader initLoader(File mappings) {
        try {
            return this.load(new TinyV1MappingsLoader(mappings, "official", "named"));
        } catch (Throwable t) {
            return this.load(new TinyV1MappingsLoader(mappings, "client", "named"));
        }
    }

    @Override
    protected boolean isReverse() {
        return false;
    }

}
