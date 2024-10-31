package net.lenni0451.sourcegen.utils.remapping.special;

import net.lenni0451.classtransform.mappings.MapperConfig;
import net.lenni0451.classtransform.mappings.impl.special.MetaTinyV2Mapper;
import net.lenni0451.classtransform.utils.ASMUtils;
import net.lenni0451.commons.io.FileUtils;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TinyV2MetadataMapper {

    private static final String COMMENT_ANNOTATION_CLASS = "net.lenni0451.sourcegen.annotations.Comment";
    private static final String COMMENT_ANNOTATION_DESC = "L" + COMMENT_ANNOTATION_CLASS + ";";
    private static final Pattern COMMENT_PATTERN = Pattern.compile("( *)@Comment\\((\\d+)\\)");

    public static void generate(final Map<String, byte[]> entries, final File mappings, final List<String> comments) {
        MetaTinyV2Mapper mapper = loadMapper(mappings);
        generate(entries, mapper.getMetadata(), comments);
    }

    public static void generate(final Map<String, byte[]> entries, final List<MetaTinyV2Mapper.ClassMetadata> metadata, final List<String> comments) {
        for (MetaTinyV2Mapper.ClassMetadata classMetadata : metadata) {
            byte[] classBytes = entries.get(classMetadata.getName() + ".class");
            if (classBytes == null) {
                throw new IllegalStateException("Class " + classMetadata.getName() + " not found in input jar");
            }
            ClassNode classNode = ASMUtils.fromBytes(classBytes);

            applyClassComment(classMetadata, classNode, comments);
            for (MetaTinyV2Mapper.FieldMetadata fieldMetadata : classMetadata.getFields()) {
                FieldNode fieldNode = ASMUtils.getField(classNode, fieldMetadata.getName(), fieldMetadata.getDescriptor());
                if (fieldNode != null) {
                    applyFieldComment(fieldMetadata, fieldNode, comments);
                }
            }
            for (MetaTinyV2Mapper.MethodMetadata methodMetadata : classMetadata.getMethods()) {
                MethodNode methodNode = ASMUtils.getMethod(classNode, methodMetadata.getName(), methodMetadata.getDescriptor());
                if (methodNode != null) {
                    applyMethodComment(methodMetadata, methodNode, comments);
                    applyParameterNames(methodMetadata, methodNode, comments);
                }
            }

            entries.put(classMetadata.getName() + ".class", ASMUtils.toStacklessBytes(classNode));
        }
    }

    public static void apply(final File source, final List<String> comments) throws IOException {
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
                comments.get(index).lines().forEach(part -> commentLines.add(spaces + " * " + part));
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

    private static MetaTinyV2Mapper loadMapper(final File mappings) {
        try {
            MetaTinyV2Mapper mapper = new MetaTinyV2Mapper(MapperConfig.create(), mappings, "official", "named");
            mapper.load();
            return mapper;
        } catch (Throwable t) {
            MetaTinyV2Mapper mapper = new MetaTinyV2Mapper(MapperConfig.create(), mappings, "client", "named");
            mapper.load();
            return mapper;
        }
    }

    private static void applyClassComment(final MetaTinyV2Mapper.ClassMetadata metadata, final ClassNode classNode, final List<String> comments) {
        if (metadata.getComment() != null) {
            comments.add(metadata.getComment().replace("\\n", "\n"));
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

    private static void applyFieldComment(final MetaTinyV2Mapper.FieldMetadata metadata, final FieldNode fieldNode, final List<String> comments) {
        if (metadata.getComment() != null) {
            comments.add(metadata.getComment().replace("\\n", "\n"));
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

    private static void applyMethodComment(final MetaTinyV2Mapper.MethodMetadata metadata, final MethodNode methodNode, final List<String> comments) {
        String comment = "";
        if (metadata.getComment() != null) comment = metadata.getComment();
        boolean hasSpace = comment.isBlank();
        for (MetaTinyV2Mapper.ParameterMetadata parameterMetadata : metadata.getParameters()) {
            if (parameterMetadata.getComment() != null) {
                if (!hasSpace) {
                    comment += "\n\n";
                    hasSpace = true;
                }
                comment += "@param " + parameterMetadata.getName() + " " + parameterMetadata.getComment();
            }
        }

        if (!comment.isBlank()) {
            comments.add(comment.replace("\\n", "\n"));
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

    private static void applyParameterNames(final MetaTinyV2Mapper.MethodMetadata metadata, final MethodNode methodNode, final List<String> comments) {
        for (MetaTinyV2Mapper.ParameterMetadata parameter : metadata.getParameters()) {
            if (methodNode.parameters != null) {
                int[] parameterIndices = ASMUtils.getParameterIndices(methodNode);
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
