package net.lenni0451.sourcegen.utils.asm.remapping;

import net.lenni0451.classtransform.additionalclassprovider.DelegatingClassProvider;
import net.lenni0451.classtransform.additionalclassprovider.MapClassProvider;
import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.mappings.MapperConfig;
import net.lenni0451.classtransform.mappings.impl.ProguardMapper;
import net.lenni0451.classtransform.utils.mappings.MapRemapper;
import net.lenni0451.classtransform.utils.mappings.SuperMappingFiller;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import net.lenni0451.classtransform.utils.tree.ClassTree;
import net.lenni0451.classtransform.utils.tree.IClassProvider;

import java.io.File;
import java.util.Map;

public class ProguardRemapper extends BaseRemapper {

    public ProguardRemapper(final File input, final File mappings, final File output) {
        super(input, mappings, output);
    }

    @Override
    protected MapRemapper loadMappings(final Map<String, byte[]> entries, final File mappings) {
        AMapper mapper = new ProguardMapper(MapperConfig.create(), mappings);
        mapper.load();
        MapRemapper remapper = mapper.getRemapper().reverse();
        ClassTree classTree = new ClassTree();
        IClassProvider classProvider = new MapClassProvider(entries, MapClassProvider.NameFormat.SLASH_CLASS);
        SuperMappingFiller.fillAllSuperMembers(remapper, classTree, new DelegatingClassProvider(classProvider, new BasicClassProvider()));
        return remapper;
    }

}
