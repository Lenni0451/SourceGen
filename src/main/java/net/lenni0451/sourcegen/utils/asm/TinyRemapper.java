package net.lenni0451.sourcegen.utils.asm;

import net.lenni0451.classtransform.additionalclassprovider.DelegatingClassProvider;
import net.lenni0451.classtransform.additionalclassprovider.MapClassProvider;
import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.mappings.MapperConfig;
import net.lenni0451.classtransform.mappings.impl.TinyV2Mapper;
import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.classtransform.utils.MemberDeclaration;
import net.lenni0451.classtransform.utils.mappings.MapRemapper;
import net.lenni0451.classtransform.utils.mappings.Remapper;
import net.lenni0451.classtransform.utils.mappings.SuperMappingFiller;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import net.lenni0451.classtransform.utils.tree.ClassTree;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import net.lenni0451.sourcegen.utils.JarUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TinyRemapper {

    private static final Pattern EXCEPTION_PATTERN1 = Pattern.compile("^([^.]+)\\.([^(]+)([^=]+)=(.*)$");
    private static final Pattern EXCEPTION_PATTERN2 = Pattern.compile("^(\\S+)/(\\S+)\\s(\\S+)\\s(.*)");

    public static void remap(final File input, final File mappings, final File exceptions, final File output) throws IOException {
        Map<String, byte[]> entries = JarUtils.read(input);
        MapRemapper remapper;
        if (mappings.exists()) {
            remapper = loadMappings(entries, mappings);
        } else {
            remapper = null;
        }

        Map<String, byte[]> out;
        if (remapper == null) out = entries;
        else out = remap(entries, remapper);
        fillExceptions(out, exceptions);
        JarUtils.write(output, out);
    }

    private static MapRemapper loadMappings(final Map<String, byte[]> entries, final File mappings) {
        AMapper mapper;
        try {
            mapper = new TinyV2Mapper(MapperConfig.create(), mappings, "named", "official");
            mapper.load();
        } catch (Throwable t) {
            mapper = new TinyV2Mapper(MapperConfig.create(), mappings, "named", "client");
            mapper.load();
        }
        MapRemapper remapper = mapper.getRemapper();
        ClassTree classTree = new ClassTree();
        IClassProvider classProvider = new MapClassProvider(entries, MapClassProvider.NameFormat.SLASH_CLASS);
        SuperMappingFiller.fillAllSuperMembers(remapper, classTree, new DelegatingClassProvider(classProvider, new BasicClassProvider()));
        return remapper.reverse();
    }

    private static Map<String, byte[]> remap(final Map<String, byte[]> entries, final MapRemapper remapper) {
        Map<String, byte[]> output = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            if (entry.getKey().toLowerCase(Locale.ROOT).endsWith(".class")) {
                String className = entry.getKey().substring(0, entry.getKey().length() - 6);
                String mappedClassName = remapper.mapSafe(className);
                if (mappedClassName.equals(className)) {
                    output.put(entry.getKey(), entry.getValue());
                } else {
                    ClassNode node = ASMUtils.fromBytes(entry.getValue());
                    node = Remapper.remap(node, remapper);
                    output.put(mappedClassName + ".class", ASMUtils.toStacklessBytes(node));
                }
            } else {
                output.put(entry.getKey(), entry.getValue());
            }
        }
        return output;
    }

    private static void fillExceptions(final Map<String, byte[]> entries, final File input) throws IOException {
        List<String> lines = Files.readAllLines(input.toPath());

        Map<MemberDeclaration, String[]> exceptions = new HashMap<>();
        for (String line : lines) {
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
