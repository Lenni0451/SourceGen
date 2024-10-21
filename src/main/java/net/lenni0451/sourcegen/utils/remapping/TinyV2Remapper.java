package net.lenni0451.sourcegen.utils.remapping;

import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.mappings.MapperConfig;
import net.lenni0451.classtransform.mappings.impl.TinyV2Mapper;

import java.io.File;
import java.util.Map;

public class TinyV2Remapper extends BaseRemapper {

    public TinyV2Remapper(final Map<String, byte[]> entries, final File mappings) {
        super(entries, mappings);
    }

    @Override
    protected AMapper loadMapper(File mappings) {
        try {
            return this.load(new TinyV2Mapper(MapperConfig.create(), mappings, "named", "official"));
        } catch (Throwable t) {
            return this.load(new TinyV2Mapper(MapperConfig.create(), mappings, "named", "client"));
        }
    }

}
