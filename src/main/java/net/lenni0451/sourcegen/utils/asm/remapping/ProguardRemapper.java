package net.lenni0451.sourcegen.utils.asm.remapping;

import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.mappings.MapperConfig;
import net.lenni0451.classtransform.mappings.impl.ProguardMapper;

import java.io.File;
import java.util.Map;

public class ProguardRemapper extends BaseRemapper {

    public ProguardRemapper(final File input, final File mappings, final File output) {
        super(input, mappings, output);
    }

    @Override
    protected AMapper loadMapper(Map<String, byte[]> entries, File mappings) {
        return new ProguardMapper(MapperConfig.create(), mappings);
    }

}
