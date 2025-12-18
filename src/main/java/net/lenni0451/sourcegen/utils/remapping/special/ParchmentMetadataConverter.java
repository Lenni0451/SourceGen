package net.lenni0451.sourcegen.utils.remapping.special;

import net.lenni0451.commons.asm.mappings.meta.ClassMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.FieldMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.MethodMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.ParameterMetaMapping;
import net.lenni0451.commons.gson.GsonParser;
import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonElement;
import net.lenni0451.commons.gson.elements.GsonObject;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParchmentMetadataConverter {

    public static List<ClassMetaMapping> toTinyV2Metadata(final File mappings) throws IOException {
        List<ClassMetaMapping> metadata = new ArrayList<>();
        GsonObject obj = GsonParser.parse(Files.readString(mappings.toPath())).asObject();
        GsonArray classes = obj.getArray("classes");
        for (GsonElement classMeta : classes) {
            metadata.add(toClassMetadata(classMeta.asObject()));
        }
        return metadata;
    }

    private static ClassMetaMapping toClassMetadata(final GsonObject obj) {
        ClassMetaMapping classMetadata = new ClassMetaMapping(obj.getString("name"), toComment(obj), new ArrayList<>(), new ArrayList<>());
        obj.optArray("fields").ifPresent(fields -> {
            for (GsonElement fieldMeta : fields) {
                classMetadata.getFields().add(toFieldMetadata(fieldMeta.asObject()));
            }
        });
        obj.optArray("methods").ifPresent(methods -> {
            for (GsonElement methodMeta : methods) {
                classMetadata.getMethods().add(toMethodMetadata(methodMeta.asObject()));
            }
        });
        return classMetadata;
    }

    private static FieldMetaMapping toFieldMetadata(final GsonObject obj) {
        return new FieldMetaMapping(obj.getString("name"), obj.getString("descriptor"), toComment(obj));
    }

    private static MethodMetaMapping toMethodMetadata(final GsonObject obj) {
        MethodMetaMapping methodMetadata = new MethodMetaMapping(obj.getString("name"), obj.getString("descriptor"), toComment(obj), new ArrayList<>());
        obj.optArray("parameters").ifPresent(parameters -> {
            for (GsonElement parameterMeta : parameters) {
                methodMetadata.getParameters().add(toParameterMetadata(parameterMeta.asObject()));
            }
        });
        return methodMetadata;
    }

    private static ParameterMetaMapping toParameterMetadata(final GsonObject obj) {
        return new ParameterMetaMapping(obj.getInt("index"), obj.getString("name"), toComment(obj));
    }

    @Nonnull
    private static String[] toComment(final GsonObject obj) {
        GsonElement rawJavadoc = obj.get("javadoc");
        if (rawJavadoc == null) return new String[0];
        String[] comment;
        if (rawJavadoc.isPrimitive()) {
            comment = new String[]{rawJavadoc.asString()};
        } else if (rawJavadoc.isArray()) {
            comment = obj.optArray("javadoc").stream().flatMap(GsonArray::stream).map(GsonElement::asString).toArray(String[]::new);
        } else {
            throw new IllegalStateException("Invalid comment type: " + rawJavadoc);
        }
        if (Arrays.stream(comment).allMatch(String::isBlank)) return new String[0];
        return comment;
    }

}
