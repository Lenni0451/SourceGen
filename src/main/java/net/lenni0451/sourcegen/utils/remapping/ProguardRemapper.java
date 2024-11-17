package net.lenni0451.sourcegen.utils.remapping;

import net.lenni0451.commons.asm.mappings.loader.MappingsLoader;
import net.lenni0451.commons.asm.mappings.loader.ProguardMappingsLoader;

import java.io.File;
import java.util.Map;

public class ProguardRemapper extends BaseRemapper {

    public ProguardRemapper(final Map<String, byte[]> entries, final File mappings) {
        super(entries, mappings);
    }

    @Override
    protected MappingsLoader initLoader(File mappings) {
        return this.load(new ProguardMappingsLoader(mappings));
    }

    @Override
    protected boolean isReverse() {
        return true;
    }

}
