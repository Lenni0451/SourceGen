package net.lenni0451.sourcegen.utils.remapping;

import lombok.SneakyThrows;
import net.lenni0451.commons.asm.info.ClassInfoProvider;
import net.lenni0451.commons.asm.info.MemberDeclaration;
import net.lenni0451.commons.asm.mappings.Mappings;
import net.lenni0451.commons.asm.mappings.MappingsFiller;
import net.lenni0451.commons.asm.mappings.Remapper;
import net.lenni0451.commons.asm.mappings.loader.MappingsLoader;
import net.lenni0451.commons.asm.provider.ClassProvider;
import net.lenni0451.commons.asm.provider.DummyClassProvider;
import net.lenni0451.commons.asm.provider.MapClassProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class BaseRemapper {

    private final Map<String, byte[]> entries;
    private final File mappings;

    public BaseRemapper(final Map<String, byte[]> entries, final File mappings) {
        this.entries = entries;
        this.mappings = mappings;
    }

    public final void remap() throws Exception {
        Mappings mappings = this.loadMappings();
        Map<String, byte[]> output = Remapper.remapJarEntries(this.entries, mappings);
        this.entries.clear();
        this.entries.putAll(output);
    }

    protected abstract MappingsLoader initLoader(final File mappings) throws Exception;

    protected abstract boolean isReverse();

    private Mappings loadMappings() throws Exception {
        MappingsLoader loader = this.initLoader(this.mappings);
        Mappings mappings = loader.getMappings();
        if (this.isReverse()) mappings = mappings.reverse(Mappings.ReverseCacheMode.STANDALONE);
        ClassProvider classProvider = new MapClassProvider(this.entries, MapClassProvider.NameFormat.SLASH_CLASS).then(new DummyClassProvider());
        ClassInfoProvider classInfoProvider = ClassInfoProvider.create(classProvider);
        MappingsFiller.fillAllSuperMembers(mappings, classInfoProvider);
        return mappings;
    }

    @SneakyThrows
    protected final <T extends MappingsLoader> T load(final T mapper) {
        mapper.load();
        return mapper;
    }

    protected final Map<String, MemberDeclaration> toReverseMappings(final Map<String, String> mappings, final Function<String, MemberDeclaration> declarationFactory) {
        Map<String, MemberDeclaration> reverseMappings = new HashMap<>();
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            reverseMappings.put(entry.getValue(), declarationFactory.apply(entry.getKey()));
        }
        return reverseMappings;
    }

}
