package net.lenni0451.sourcegen.utils.remapping;

import net.lenni0451.commons.asm.mappings.loader.MappingsProvider;
import net.lenni0451.commons.asm.mappings.loader.formats.TinyV1MappingsLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TinyV1Remapper extends BaseRemapper {

    private final TinyNamespace[] namespaces;

    public TinyV1Remapper(final Map<String, byte[]> entries, final File mappings, final TinyNamespace initialNamespace, final TinyNamespace... namespaces) {
        super(entries, mappings);
        this.namespaces = TinyNamespace.merge(initialNamespace, namespaces);
    }

    @Override
    protected MappingsProvider initProvider(File mappings) {
        List<Throwable> tries = new ArrayList<>();
        for (TinyNamespace namespace : this.namespaces) {
            try {
                return this.load(new TinyV1MappingsLoader(mappings, namespace.from(), namespace.to()));
            } catch (Throwable t) {
                tries.add(t);
            }
        }
        IllegalStateException e = new IllegalStateException("Failed to load TinyV1 mappings with known namespaces");
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
