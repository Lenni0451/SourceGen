package net.lenni0451.sourcegen.utils.remapping;

import net.lenni0451.classtransform.additionalclassprovider.DelegatingClassProvider;
import net.lenni0451.classtransform.additionalclassprovider.MapClassProvider;
import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.classtransform.utils.mappings.MapRemapper;
import net.lenni0451.classtransform.utils.mappings.Remapper;
import net.lenni0451.classtransform.utils.mappings.SuperMappingFiller;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import net.lenni0451.classtransform.utils.tree.ClassTree;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import net.lenni0451.sourcegen.utils.asm.DummyClassProvider;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class BaseRemapper {

    private final Map<String, byte[]> entries;
    private final File mappings;

    public BaseRemapper(final Map<String, byte[]> entries, final File mappings) {
        this.entries = entries;
        this.mappings = mappings;
    }

    public final void remap() throws Exception {
        MapRemapper remapper = this.loadMappings(this.entries, this.mappings);
        this.remap(remapper);
    }

    protected abstract AMapper loadMapper(final File mappings) throws Exception;

    private MapRemapper loadMappings(final Map<String, byte[]> entries, final File mappings) throws Exception {
        AMapper mapper = this.loadMapper(mappings);
        MapRemapper remapper = mapper.getRemapper();
        ClassTree classTree = new ClassTree();
        IClassProvider classProvider = new DelegatingClassProvider(new MapClassProvider(entries, MapClassProvider.NameFormat.SLASH_CLASS), new DummyClassProvider());
        SuperMappingFiller.fillAllSuperMembers(remapper, classTree, new DelegatingClassProvider(classProvider, new BasicClassProvider()));
        return remapper.reverse();
    }

    private void remap(final MapRemapper remapper) {
        Map<String, byte[]> output = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : this.entries.entrySet()) {
            if (entry.getKey().toLowerCase(Locale.ROOT).endsWith(".class")) {
                ClassNode node = ASMUtils.fromBytes(entry.getValue());
                node = Remapper.remap(node, remapper);
                output.put(node.name + ".class", ASMUtils.toStacklessBytes(node));
            } else {
                output.put(entry.getKey(), entry.getValue());
            }
        }
        this.entries.clear();
        this.entries.putAll(output);
    }

    protected final <T extends AMapper> T load(final T mapper) {
        mapper.load();
        return mapper;
    }

}
