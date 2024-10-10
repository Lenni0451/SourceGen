package net.lenni0451.sourcegen.utils.asm;

import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Supplier;

public class DummyClassProvider implements IClassProvider {

    @Nonnull
    @Override
    public byte[] getClass(@NotNull String name) {
        ClassNode dummyClass = ASMUtils.createEmptyClass(name);
        return ASMUtils.toStacklessBytes(dummyClass);
    }

    @Nonnull
    @Override
    public Map<String, Supplier<byte[]>> getAllClasses() {
        throw new UnsupportedOperationException();
    }

}
