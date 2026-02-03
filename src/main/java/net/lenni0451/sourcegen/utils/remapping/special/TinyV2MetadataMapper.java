package net.lenni0451.sourcegen.utils.remapping.special;

import net.lenni0451.commons.asm.ASMUtils;
import net.lenni0451.commons.asm.io.ClassIO;
import net.lenni0451.commons.asm.mappings.loader.formats.TinyV2MappingsLoader;
import net.lenni0451.commons.asm.mappings.meta.ClassMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.FieldMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.MethodMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.ParameterMetaMapping;
import net.lenni0451.commons.io.FileUtils;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class TinyV2MetadataMapper {

    private static final String COMMENT_ANNOTATION_CLASS = "net.lenni0451.sourcegen.annotations.Comment";
    private static final String COMMENT_ANNOTATION_DESC = "L" + COMMENT_ANNOTATION_CLASS + ";";

    public static void generate(final Map<String, byte[]> entries, final File mappings) {
        TinyV2MappingsLoader mapper = loadMapper(mappings);
        generate(entries, mapper.getMetaMappings());
    }

    public static void generate(final Map<String, byte[]> entries, final List<ClassMetaMapping> metadata) {
        for (ClassMetaMapping classMetadata : metadata) {
            byte[] classBytes = entries.get(classMetadata.getName() + ".class");
            if (classBytes == null) {
                System.err.println("Class " + classMetadata.getName() + " not found in input jar");
                continue;
            }
            ClassNode classNode = ClassIO.fromBytes(classBytes);

            applyClassComment(classMetadata, classNode);
            for (FieldMetaMapping fieldMetadata : classMetadata.getFields()) {
                FieldNode fieldNode = ASMUtils.getField(classNode, fieldMetadata.getName(), fieldMetadata.getDescriptor());
                if (fieldNode != null) {
                    applyFieldComment(fieldMetadata, fieldNode);
                }
            }
            for (MethodMetaMapping methodMetadata : classMetadata.getMethods()) {
                MethodNode methodNode = ASMUtils.getMethod(classNode, methodMetadata.getName(), methodMetadata.getDescriptor());
                if (methodNode != null) {
                    applyMethodComment(methodMetadata, methodNode);
                    applyParameterNames(methodMetadata, methodNode);
                }
            }

            entries.put(classMetadata.getName() + ".class", ClassIO.toStacklessBytes(classNode));
        }
    }

    public static void apply(final File source) throws IOException {
        for (File file : FileUtils.listFiles(source)) {
            if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".java")) continue;
            List<String> lines = Files.readAllLines(file.toPath());
            List<String> output = new ArrayList<>(lines.size());
            boolean modified = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains(COMMENT_ANNOTATION_CLASS)) {
                    modified = true;
                    continue;
                } else if (line.contains("@Comment")) {
                    modified = true;
                    if (!line.contains("@Comment(")) {
                        throw new IllegalStateException("Invalid @Comment annotation format in file " + file.getAbsolutePath() + ": " + line);
                    }
                    if (line.endsWith("(")) {
                        //Multiline annotation
                        if (i + 2 >= lines.size()) {
                            throw new IllegalStateException("Invalid @Comment annotation format in file " + file.getAbsolutePath() + ": " + line);
                        }
                        String nextLine = lines.get(i + 1).trim();
                        String endLine = lines.get(i + 2).trim();
                        if (!nextLine.startsWith("\"") || !nextLine.endsWith("\"") || !endLine.equals(")")) {
                            throw new IllegalStateException("Invalid @Comment annotation format in file " + file.getAbsolutePath() + ": " + line + " " + nextLine + " " + endLine);
                        }

                        String spaces = line.substring(0, line.indexOf("@Comment"));
                        String[] commentLines = new String(Base64.getDecoder().decode(nextLine.substring(1, nextLine.length() - 1)), StandardCharsets.UTF_8).split("\n");
                        output.add(spaces + "/**");
                        for (String commentLine : commentLines) {
                            output.add(spaces + " * " + commentLine);
                        }
                        output.add(spaces + " */");
                        i += 2;
                    } else if (line.endsWith(")")) {
                        String spaces = line.substring(0, line.indexOf("@Comment"));
                        String[] commentLines = new String(Base64.getDecoder().decode(line.substring(line.indexOf("(\"") + 2, line.lastIndexOf("\")"))), StandardCharsets.UTF_8).split("\n");
                        output.add(spaces + "/**");
                        for (String commentLine : commentLines) {
                            output.add(spaces + " * " + commentLine);
                        }
                        output.add(spaces + " */");
                    }
                } else {
                    output.add(line);
                }
            }
            if (modified) {
                Files.write(file.toPath(), output);
            }
        }
    }

    private static TinyV2MappingsLoader loadMapper(final File mappings) {
        List<Throwable> tries = new ArrayList<>();
        try {
            TinyV2MappingsLoader mapper = new TinyV2MappingsLoader(mappings, "official", "named").enableMetaParsing();
            mapper.load();
            return mapper;
        } catch (Throwable t) {
            tries.add(t);
        }
        try {
            TinyV2MappingsLoader mapper = new TinyV2MappingsLoader(mappings, "client", "named").enableMetaParsing();
            mapper.load();
            return mapper;
        } catch (Throwable t) {
            tries.add(t);
        }
        try {
            TinyV2MappingsLoader mapper = new TinyV2MappingsLoader(mappings, "clientOfficial", "named").enableMetaParsing();
            mapper.load();
            return mapper;
        } catch (Throwable t) {
            tries.add(t);
        }
        IllegalStateException e = new IllegalStateException("Failed to load TinyV2 mappings with known namespaces");
        for (Throwable t : tries) {
            e.addSuppressed(t);
        }
        throw e;
    }

    private static void applyClassComment(final ClassMetaMapping metadata, final ClassNode classNode) {
        if (metadata.hasJavadoc()) {
            classNode.visibleAnnotations = addAnnotation(classNode.visibleAnnotations, metadata.getJavadoc());
        }
    }

    private static void applyFieldComment(final FieldMetaMapping metadata, final FieldNode fieldNode) {
        if (metadata.hasJavadoc()) {
            fieldNode.visibleAnnotations = addAnnotation(fieldNode.visibleAnnotations, metadata.getJavadoc());
        }
    }

    private static void applyMethodComment(final MethodMetaMapping metadata, final MethodNode methodNode) {
        List<String> commentLines = new ArrayList<>();
        if (metadata.hasJavadoc()) Collections.addAll(commentLines, metadata.getJavadoc());
        boolean requiresSpace = !commentLines.isEmpty();
        for (ParameterMetaMapping parameterMetaMapping : metadata.getParameters()) {
            if (parameterMetaMapping.hasJavadoc()) {
                if (requiresSpace) {
                    commentLines.add("");
                    requiresSpace = false;
                }
                if (!parameterMetaMapping.getName().isBlank()) {
                    commentLines.add("@param " + parameterMetaMapping.getName() + " " + String.join("<br>", parameterMetaMapping.getJavadoc()));
                }
            }
        }

        if (!commentLines.isEmpty()) {
            methodNode.visibleAnnotations = addAnnotation(methodNode.visibleAnnotations, commentLines.toArray(new String[0]));
        }
    }

    private static void applyParameterNames(final MethodMetaMapping metadata, final MethodNode methodNode) {
        for (ParameterMetaMapping parameter : metadata.getParameters()) {
            if (parameter.getName().isBlank()) continue;
            if (methodNode.parameters != null) {
                int[] parameterIndices = ASMUtils.parameterIndices(methodNode);
                if (methodNode.parameters.size() == parameterIndices.length) {
                    for (int i = 0; i < parameterIndices.length; i++) {
                        if (parameter.getIndex() == parameterIndices[i]) {
                            methodNode.parameters.get(i).name = parameter.getName();
                            break;
                        }
                    }
                }
            }
            if (methodNode.localVariables != null) {
                for (LocalVariableNode localVariable : methodNode.localVariables) {
                    if (localVariable.index == parameter.getIndex()) {
                        localVariable.name = parameter.getName();
                        break;
                    }
                }
            }
        }
    }

    private static List<AnnotationNode> addAnnotation(List<AnnotationNode> annotations, final String[] lines) {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        AnnotationNode commentAnnotation = new AnnotationNode(COMMENT_ANNOTATION_DESC);
        commentAnnotation.values = new ArrayList<>();
        commentAnnotation.values.add("value");
        commentAnnotation.values.add(Base64.getEncoder().encodeToString(String.join("\n", lines).getBytes(StandardCharsets.UTF_8)));
        annotations.add(0, commentAnnotation);
        return annotations;
    }

}
