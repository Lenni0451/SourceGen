package net.lenni0451.sourcegen.utils.remapping;

import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.mappings.MapperConfig;
import net.lenni0451.classtransform.mappings.impl.ProguardMapper;

import java.io.File;
import java.util.Map;

public class ProguardRemapper extends BaseRemapper {

    public ProguardRemapper(final Map<String, byte[]> entries, final File mappings) {
        super(entries, mappings);
    }

    @Override
    protected AMapper loadMapper(File mappings) {
        return this.load(new ProguardMapper(MapperConfig.create(), mappings));
    }

}
