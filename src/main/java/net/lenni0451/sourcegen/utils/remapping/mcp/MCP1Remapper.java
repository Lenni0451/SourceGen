package net.lenni0451.sourcegen.utils.remapping.mcp;

import net.lenni0451.commons.asm.mappings.loader.MappingsLoader;
import net.lenni0451.commons.asm.mappings.loader.RetroguardMappingsLoader;
import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.utils.remapping.BaseRemapper;

import java.io.File;
import java.util.Map;

public class MCP1Remapper extends BaseRemapper {

    public MCP1Remapper(final Map<String, byte[]> entries, final File mappings) {
        super(entries, mappings);
    }

    @Override
    protected MappingsLoader initLoader(File mappings) {
        return this.load(new RetroguardMappingsLoader(FileUtils.create(mappings, "conf", "minecraft.rgs")));
    }

    @Override
    protected boolean isReverse() {
        return false;
    }

}
