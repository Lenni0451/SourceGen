package net.lenni0451.sourcegen.utils.remapping;

import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.mappings.MapperConfig;
import net.lenni0451.classtransform.mappings.impl.TinyV2Mapper;
import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.classtransform.utils.MemberDeclaration;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TinyV2Remapper extends BaseRemapper {

    private static final Pattern EXCEPTION_PATTERN1 = Pattern.compile("^([^.]+)\\.([^(]+)([^=]+)=(.*)$");
    private static final Pattern EXCEPTION_PATTERN2 = Pattern.compile("^(\\S+)/(\\S+)\\s(\\S+)\\s(.*)");

    private final File exceptions;

    public TinyV2Remapper(final File input, final File mappings, final File output, final File exceptions) {
        super(input, mappings, output);
        this.exceptions = exceptions;
    }

    @Override
    protected void postRemap(Map<String, byte[]> out) throws IOException {
        if (this.exceptions != null && this.exceptions.exists()) this.fillExceptions(out);
    }

    @Override
    protected AMapper loadMapper(Map<String, byte[]> entries, File mappings) {
        try {
            return this.load(new TinyV2Mapper(MapperConfig.create(), mappings, "named", "official"));
        } catch (Throwable t) {
            return this.load(new TinyV2Mapper(MapperConfig.create(), mappings, "named", "client"));
        }
    }

    private void fillExceptions(final Map<String, byte[]> entries) throws IOException {
        List<String> lines = Files.readAllLines(this.exceptions.toPath());

        Map<MemberDeclaration, String[]> exceptions = new HashMap<>();
        for (String line : lines) {
            if (line.isBlank()) continue;
            Matcher matcher = EXCEPTION_PATTERN1.matcher(line);
            if (!matcher.matches()) {
                matcher = EXCEPTION_PATTERN2.matcher(line);
                if (!matcher.matches()) throw new IllegalArgumentException("Invalid exception line: " + line);
            }
            String className = matcher.group(1);
            String methodName = matcher.group(2);
            String methodDesc = matcher.group(3);
            String[] exceptionNames = matcher.group(4).split(",");
            exceptions.put(new MemberDeclaration(className, methodName, methodDesc), exceptionNames);
        }
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            if (!entry.getKey().toLowerCase(Locale.ROOT).endsWith(".class")) continue;
            ClassNode node = ASMUtils.fromBytes(entry.getValue());
            boolean modified = false;
            for (MethodNode method : node.methods) {
                String[] exceptionNames = exceptions.get(new MemberDeclaration(node.name, method.name, method.desc));
                if (exceptionNames != null) {
                    method.exceptions = Arrays.asList(exceptionNames);
                    modified = true;
                }
            }
            if (modified) entry.setValue(ASMUtils.toStacklessBytes(node));
        }
    }

}
