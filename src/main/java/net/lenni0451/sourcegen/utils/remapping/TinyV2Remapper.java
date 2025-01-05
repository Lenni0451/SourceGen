package net.lenni0451.sourcegen.utils.remapping;

import net.lenni0451.commons.asm.mappings.loader.MappingsLoader;
import net.lenni0451.commons.asm.mappings.loader.TinyV2MappingsLoader;

import java.io.File;
import java.util.Map;

public class TinyV2Remapper extends BaseRemapper {

    public TinyV2Remapper(final Map<String, byte[]> entries, final File mappings) {
        super(entries, mappings);
    }

    @Override
    protected MappingsLoader initLoader(File mappings) {
        try {
            return this.load(new TinyV2MappingsLoader(mappings, "official", "named"));
        } catch (Throwable t) {
            try {
                return this.load(new TinyV2MappingsLoader(mappings, "client", "named"));
            } catch (Throwable t2) {
                RuntimeException e = new RuntimeException("Failed to load mappings", t);
                e.addSuppressed(t2);
                throw e;
            }
        }
    }

    @Override
    protected boolean isReverse() {
        return false;
    }

}
