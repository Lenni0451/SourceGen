package net.lenni0451.sourcegen.utils.remapping.special;

import net.lenni0451.commons.asm.ASMUtils;
import net.lenni0451.commons.asm.io.ClassIO;
import net.lenni0451.commons.asm.mappings.loader.TinyV2MappingsLoader;
import net.lenni0451.commons.asm.mappings.meta.ClassMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.FieldMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.MethodMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.ParameterMetaMapping;
import net.lenni0451.commons.io.FileUtils;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TinyV2MetadataMapper {

    private static final String COMMENT_ANNOTATION_CLASS = "net.lenni0451.sourcegen.annotations.Comment";
    private static final String COMMENT_ANNOTATION_DESC = "L" + COMMENT_ANNOTATION_CLASS + ";";
    private static final Pattern COMMENT_PATTERN = Pattern.compile("( *)@Comment\\((\\d+)\\)");

    public static void generate(final Map<String, byte[]> entries, final File mappings, final List<String[]> comments) {
        TinyV2MappingsLoader mapper = loadMapper(mappings);
        generate(entries, mapper.getMetaMappings(), comments);
    }

    public static void generate(final Map<String, byte[]> entries, final List<ClassMetaMapping> metadata, final List<String[]> comments) {
        for (ClassMetaMapping classMetadata : metadata) {
            byte[] classBytes = entries.get(classMetadata.getName() + ".class");
            if (classBytes == null) {
                System.err.println("Class " + classMetadata.getName() + " not found in input jar");
                continue;
            }
            ClassNode classNode = ClassIO.fromBytes(classBytes);

            applyClassComment(classMetadata, classNode, comments);
            for (FieldMetaMapping fieldMetadata : classMetadata.getFields()) {
                FieldNode fieldNode = ASMUtils.getField(classNode, fieldMetadata.getName(), fieldMetadata.getDescriptor());
                if (fieldNode != null) {
                    applyFieldComment(fieldMetadata, fieldNode, comments);
                }
            }
            for (MethodMetaMapping methodMetadata : classMetadata.getMethods()) {
                MethodNode methodNode = ASMUtils.getMethod(classNode, methodMetadata.getName(), methodMetadata.getDescriptor());
                if (methodNode != null) {
                    applyMethodComment(methodMetadata, methodNode, comments);
                    applyParameterNames(methodMetadata, methodNode, comments);
                }
            }

            entries.put(classMetadata.getName() + ".class", ClassIO.toStacklessBytes(classNode));
        }
    }

    public static void apply(final File source, final List<String[]> comments) throws IOException {
        for (File file : FileUtils.listFiles(source)) {
            if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".java")) continue;
            List<String> lines = Files.readAllLines(file.toPath());
            int startSize = lines.size();
            lines.removeIf(line -> line.contains(COMMENT_ANNOTATION_CLASS)); //Remove imports of the comment annotation
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                Matcher matcher = COMMENT_PATTERN.matcher(line);
                if (!matcher.find()) {
                    if (line.contains("@Comment")) {
                        throw new IllegalStateException("Comment annotation without value in " + file.getName() + " at line " + (i + 1));
                    }
                    continue;
                }

                String spaces = matcher.group(1);
                int index = Integer.parseInt(matcher.group(2));
                if (index < 0 || index >= comments.size()) {
                    throw new IllegalStateException("Comment index out of bounds in " + file.getName() + " at line " + (i + 1));
                }

                lines.remove(i);
                List<String> commentLines = new ArrayList<>();
                commentLines.add(spaces + "/**");
                for (String commentLine : comments.get(index)) {
                    commentLines.add(spaces + " * " + commentLine);
                }
                commentLines.add(spaces + " */");
                for (int j = commentLines.size() - 1; j >= 0; j--) {
                    lines.add(i, commentLines.get(j));
                }
                i += commentLines.size();
            }
            if (lines.size() != startSize) {
                //Only write the file if it was changed
                Files.write(file.toPath(), lines);
            }
        }
    }

    private static TinyV2MappingsLoader loadMapper(final File mappings) {
        try {
            TinyV2MappingsLoader mapper = new TinyV2MappingsLoader(mappings, "official", "named").enableMetaParsing();
            mapper.load();
            return mapper;
        } catch (Throwable t) {
            try {
                TinyV2MappingsLoader mapper = new TinyV2MappingsLoader(mappings, "client", "named").enableMetaParsing();
                mapper.load();
                return mapper;
            } catch (Throwable t2) {
                RuntimeException e = new RuntimeException("Failed to load mappings", t);
                e.addSuppressed(t2);
                throw e;
            }
        }
    }

    private static void applyClassComment(final ClassMetaMapping metadata, final ClassNode classNode, final List<String[]> comments) {
        if (metadata.hasJavadoc()) {
            comments.add(metadata.getJavadoc());
            List<AnnotationNode> visibleAnnotations = classNode.visibleAnnotations;
            if (visibleAnnotations == null) {
                visibleAnnotations = new ArrayList<>();
                classNode.visibleAnnotations = visibleAnnotations;
            }
            AnnotationNode commentAnnotation = new AnnotationNode(COMMENT_ANNOTATION_DESC);
            commentAnnotation.values = new ArrayList<>();
            commentAnnotation.values.add("value");
            commentAnnotation.values.add(comments.size() - 1);
            visibleAnnotations.add(0, commentAnnotation);
        }
    }

    private static void applyFieldComment(final FieldMetaMapping metadata, final FieldNode fieldNode, final List<String[]> comments) {
        if (metadata.hasJavadoc()) {
            comments.add(metadata.getJavadoc());
            List<AnnotationNode> visibleAnnotations = fieldNode.visibleAnnotations;
            if (visibleAnnotations == null) {
                visibleAnnotations = new ArrayList<>();
                fieldNode.visibleAnnotations = visibleAnnotations;
            }
            AnnotationNode commentAnnotation = new AnnotationNode(COMMENT_ANNOTATION_DESC);
            commentAnnotation.values = new ArrayList<>();
            commentAnnotation.values.add("value");
            commentAnnotation.values.add(comments.size() - 1);
            visibleAnnotations.add(0, commentAnnotation);
        }
    }

    private static void applyMethodComment(final MethodMetaMapping metadata, final MethodNode methodNode, final List<String[]> comments) {
        List<String> commentLines = new ArrayList<>();
        if (metadata.hasJavadoc()) Collections.addAll(commentLines, metadata.getJavadoc());
        boolean requiresSpace = !commentLines.isEmpty();
        for (ParameterMetaMapping parameterMetaMapping : metadata.getParameters()) {
            if (parameterMetaMapping.hasJavadoc()) {
                if (requiresSpace) {
                    commentLines.add("");
                    requiresSpace = false;
                }
                commentLines.add("@param " + parameterMetaMapping.getName() + " " + String.join("<br>", parameterMetaMapping.getJavadoc()));
            }
        }

        if (!comments.isEmpty()) {
            comments.add(commentLines.toArray(new String[0]));
            List<AnnotationNode> visibleAnnotations = methodNode.visibleAnnotations;
            if (visibleAnnotations == null) {
                visibleAnnotations = new ArrayList<>();
                methodNode.visibleAnnotations = visibleAnnotations;
            }
            AnnotationNode commentAnnotation = new AnnotationNode(COMMENT_ANNOTATION_DESC);
            commentAnnotation.values = new ArrayList<>();
            commentAnnotation.values.add("value");
            commentAnnotation.values.add(comments.size() - 1);
            visibleAnnotations.add(0, commentAnnotation);
        }
    }

    private static void applyParameterNames(final MethodMetaMapping metadata, final MethodNode methodNode, final List<String[]> comments) {
        for (ParameterMetaMapping parameter : metadata.getParameters()) {
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

}
