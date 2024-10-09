package net.lenni0451.sourcegen.utils.asm.remapping;

import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.classtransform.utils.mappings.MapRemapper;
import net.lenni0451.classtransform.utils.mappings.Remapper;
import net.lenni0451.sourcegen.utils.JarUtils;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class BaseRemapper {

    private final File input;
    private final File mappings;
    private final File output;

    public BaseRemapper(final File input, final File mappings, final File output) {
        this.input = input;
        this.mappings = mappings;
        this.output = output;
    }

    public void remap() throws Exception {
        Map<String, byte[]> entries = JarUtils.read(this.input);
        MapRemapper remapper;
        if (this.mappings.exists()) {
            remapper = loadMappings(entries, this.mappings);
        } else {
            remapper = null;
        }

        Map<String, byte[]> out;
        if (remapper == null) {
            out = entries;
        } else {
            out = remap(entries, remapper);
        }
        this.postRemap(out);
        JarUtils.write(this.output, out);
    }

    protected abstract MapRemapper loadMappings(final Map<String, byte[]> entries, final File mappings) throws Exception;

    protected void postRemap(final Map<String, byte[]> out) throws Exception {
    }

    protected Map<String, byte[]> remap(final Map<String, byte[]> entries, final MapRemapper remapper) {
        Map<String, byte[]> output = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            if (entry.getKey().toLowerCase(Locale.ROOT).endsWith(".class")) {
                ClassNode node = ASMUtils.fromBytes(entry.getValue());
                node = Remapper.remap(node, remapper);
                output.put(node.name + ".class", ASMUtils.toStacklessBytes(node));
            } else {
                output.put(entry.getKey(), entry.getValue());
            }
        }
        return output;
    }

}
