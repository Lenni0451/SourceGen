package net.lenni0451.sourcegen.utils.remapping;

import net.lenni0451.commons.asm.mappings.loader.MappingsProvider;
import net.lenni0451.commons.asm.mappings.loader.formats.TinyV2MappingsLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TinyV2Remapper extends BaseRemapper {

    public TinyV2Remapper(final Map<String, byte[]> entries, final File mappings) {
        super(entries, mappings);
    }

    @Override
    protected MappingsProvider initProvider(File mappings) {
        List<Throwable> tries = new ArrayList<>();
        try {
            return this.load(new TinyV2MappingsLoader(mappings, "official", "named"));
        } catch (Throwable t) {
            tries.add(t);
        }
        try {
            return this.load(new TinyV2MappingsLoader(mappings, "client", "named"));
        } catch (Throwable t) {
            tries.add(t);
        }
        try {
            return this.load(new TinyV2MappingsLoader(mappings, "clientOfficial", "named"));
        } catch (Throwable t) {
            tries.add(t);
        }
        IllegalStateException e = new IllegalStateException("Failed to load TinyV2 mappings with known namespaces");
        for (Throwable t : tries) {
            e.addSuppressed(t);
        }
        throw e;
    }

    @Override
    protected boolean isReverse() {
        return false;
    }

}
