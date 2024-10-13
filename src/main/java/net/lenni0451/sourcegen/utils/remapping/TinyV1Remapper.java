package net.lenni0451.sourcegen.utils.remapping;

import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.mappings.MapperConfig;
import net.lenni0451.classtransform.mappings.impl.TinyV1Mapper;

import java.io.File;
import java.util.Map;

public class TinyV1Remapper extends BaseRemapper {

    public TinyV1Remapper(final File input, final File mappings, final File output) {
        super(input, mappings, output);
    }

    @Override
    protected AMapper loadMapper(Map<String, byte[]> entries, File mappings) {
        try {
            TinyV1Mapper mapper = new TinyV1Mapper(MapperConfig.create(), mappings, "named", "official");
            mapper.load();
            return mapper;
        } catch (Throwable t) {
            TinyV1Mapper mapper = new TinyV1Mapper(MapperConfig.create(), mappings, "named", "client");
            mapper.load();
            return mapper;
        }
    }

}
