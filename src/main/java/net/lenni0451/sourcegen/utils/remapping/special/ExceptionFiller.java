package net.lenni0451.sourcegen.utils.remapping.special;

import net.lenni0451.commons.asm.info.MemberDeclaration;
import net.lenni0451.commons.asm.io.ClassIO;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionFiller {

    private static final Pattern EXCEPTION_PATTERN1 = Pattern.compile("^([^.]+)\\.([^(]+)([^=]+)=(.*)$");
    private static final Pattern EXCEPTION_PATTERN2 = Pattern.compile("^(\\S+)/(\\S+)\\s(\\S+)\\s(.*)");

    public static void run(final Map<String, byte[]> entries, final File exceptionsFile) throws IOException {
        List<String> lines = Files.readAllLines(exceptionsFile.toPath());

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
            ClassNode node = ClassIO.fromBytes(entry.getValue());
            boolean modified = false;
            for (MethodNode method : node.methods) {
                String[] exceptionNames = exceptions.get(new MemberDeclaration(node.name, method.name, method.desc));
                if (exceptionNames != null) {
                    method.exceptions = Arrays.asList(exceptionNames);
                    modified = true;
                }
            }
            if (modified) entry.setValue(ClassIO.toStacklessBytes(node));
        }
    }

}
