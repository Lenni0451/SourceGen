package net.lenni0451.sourcegen.utils.remapping.special;

import net.lenni0451.commons.asm.mappings.meta.ClassMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.FieldMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.MethodMetaMapping;
import net.lenni0451.commons.asm.mappings.meta.ParameterMetaMapping;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParchmentMetadataConverter {

    private static final JSONArray EMPTY_JSON_ARRAY = new JSONArray();

    public static List<ClassMetaMapping> toTinyV2Metadata(final File mappings) throws IOException {
        List<ClassMetaMapping> metadata = new ArrayList<>();
        JSONObject obj = new JSONObject(new JSONTokener(Files.readString(mappings.toPath())));
        JSONArray classes = obj.getJSONArray("classes");
        for (Object classMeta : classes) {
            metadata.add(toClassMetadata((JSONObject) classMeta));
        }
        return metadata;
    }

    private static ClassMetaMapping toClassMetadata(final JSONObject obj) {
        ClassMetaMapping classMetadata = new ClassMetaMapping(obj.getString("name"), toComment(obj), new ArrayList<>(), new ArrayList<>());
        for (Object fieldMeta : obj.optJSONArray("fields", EMPTY_JSON_ARRAY)) {
            classMetadata.getFields().add(toFieldMetadata((JSONObject) fieldMeta));
        }
        for (Object methodMeta : obj.optJSONArray("methods", EMPTY_JSON_ARRAY)) {
            classMetadata.getMethods().add(toMethodMetadata((JSONObject) methodMeta));
        }
        return classMetadata;
    }

    private static FieldMetaMapping toFieldMetadata(final JSONObject obj) {
        return new FieldMetaMapping(obj.getString("name"), obj.getString("descriptor"), toComment(obj));
    }

    private static MethodMetaMapping toMethodMetadata(final JSONObject obj) {
        MethodMetaMapping methodMetadata = new MethodMetaMapping(obj.getString("name"), obj.getString("descriptor"), toComment(obj), new ArrayList<>());
        for (Object parameterMeta : obj.optJSONArray("parameters", EMPTY_JSON_ARRAY)) {
            methodMetadata.getParameters().add(toParameterMetadata((JSONObject) parameterMeta));
        }
        return methodMetadata;
    }

    private static ParameterMetaMapping toParameterMetadata(final JSONObject obj) {
        return new ParameterMetaMapping(obj.getInt("index"), obj.getString("name"), toComment(obj));
    }

    @Nullable
    private static String[] toComment(final JSONObject obj) {
        Object rawJavadoc = obj.opt("javadoc");
        if (rawJavadoc == null) return null;
        String[] comment;
        if (rawJavadoc instanceof String) {
            comment = new String[]{(String) rawJavadoc};
        } else if (rawJavadoc instanceof JSONArray) {
            comment = obj.optJSONArray("javadoc", EMPTY_JSON_ARRAY).toList().stream().map(Object::toString).toArray(String[]::new);
        } else {
            throw new IllegalStateException("Invalid comment type: " + rawJavadoc);
        }
        if (Arrays.stream(comment).allMatch(String::isBlank)) return null;
        return comment;
    }

}
