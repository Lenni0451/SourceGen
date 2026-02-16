package net.lenni0451.sourcegen.utils.remapping.special;

import net.lenni0451.commons.asm.ASMUtils;
import net.lenni0451.commons.asm.io.ClassIO;
import net.lenni0451.commons.asm.mappings.loader.formats.TinyV2MappingsLoader;
import net.lenni0451.commons.asm.mappings.meta.ClassMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.FieldMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.MethodMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.ParameterMetaMapping;
import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.utils.remapping.TinyNamespace;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

public class TinyV2MetadataMapper {

    private static final String COMMENT_ANNOTATION_CLASS = "net.lenni0451.sourcegen.annotations.Comment";
    private static final String COMMENT_ANNOTATION_DESC = "L" + COMMENT_ANNOTATION_CLASS + ";";
    private static final Pattern COMMENT_ANNOTATION_PATTERN = Pattern.compile("([ \\t]*)@Comment\\s*\\(\\s*\"([^\"]+)\"\\s*\\)");

    public static void generate(final Map<String, byte[]> entries, final File mappings, final TinyNamespace initialNamespace, final TinyNamespace... namespaces) {
        TinyV2MappingsLoader mapper = loadMapper(mappings, TinyNamespace.merge(initialNamespace, namespaces));
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
            String content = Files.readString(file.toPath());
            if (!content.contains(COMMENT_ANNOTATION_CLASS)) continue;

            content = content.replace("import " + COMMENT_ANNOTATION_CLASS + ";\n", "");
            content = COMMENT_ANNOTATION_PATTERN.matcher(content).replaceAll(result -> {
                String spaces = result.group(1);
                String[] commentLines = new String(Base64.getDecoder().decode(result.group(2).replaceAll("\\s","")), StandardCharsets.UTF_8).split("\n");
                StringBuilder sb = new StringBuilder();
                sb.append(spaces).append("/**\n");
                for (String commentLine : commentLines) {
                    sb.append(spaces).append(" * ").append(commentLine).append("\n");
                }
                sb.append(spaces).append(" */");
                return sb.toString();
            });
            if (content.contains("@Comment")) {
                throw new IllegalStateException("Invalid @Comment annotation format in file " + file.getAbsolutePath() + ": " + content);
            }
            Files.writeString(file.toPath(), content);
        }
    }

    private static TinyV2MappingsLoader loadMapper(final File mappings, final TinyNamespace[] namespaces) {
        List<Throwable> tries = new ArrayList<>();
        for (TinyNamespace namespace : namespaces) {
            try {
                TinyV2MappingsLoader mapper = new TinyV2MappingsLoader(mappings, namespace.from(), namespace.to()).enableMetaParsing();
                mapper.load();
                return mapper;
            } catch (Throwable t) {
                tries.add(t);
            }
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
