package net.lenni0451.sourcegen.utils.remapping.special;

import net.lenni0451.classtransform.mappings.impl.special.MetaTinyV2Mapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ParchmentMetadataConverter {

    private static final JSONArray EMPTY_JSON_ARRAY = new JSONArray();

    public static List<MetaTinyV2Mapper.ClassMetadata> toTinyV2Metadata(final File mappings) throws IOException {
        List<MetaTinyV2Mapper.ClassMetadata> metadata = new ArrayList<>();
        JSONObject obj = new JSONObject(new JSONTokener(Files.readString(mappings.toPath())));
        JSONArray classes = obj.getJSONArray("classes");
        for (Object classMeta : classes) {
            metadata.add(toClassMetadata((JSONObject) classMeta));
        }
        return metadata;
    }

    private static MetaTinyV2Mapper.ClassMetadata toClassMetadata(final JSONObject obj) {
        MetaTinyV2Mapper.ClassMetadata classMetadata = new MetaTinyV2Mapper.ClassMetadata(obj.getString("name"), toComment(obj), new ArrayList<>(), new ArrayList<>());
        for (Object fieldMeta : obj.optJSONArray("fields", EMPTY_JSON_ARRAY)) {
            classMetadata.getFields().add(toFieldMetadata((JSONObject) fieldMeta));
        }
        for (Object methodMeta : obj.optJSONArray("methods", EMPTY_JSON_ARRAY)) {
            classMetadata.getMethods().add(toMethodMetadata((JSONObject) methodMeta));
        }
        return classMetadata;
    }

    private static MetaTinyV2Mapper.FieldMetadata toFieldMetadata(final JSONObject obj) {
        return new MetaTinyV2Mapper.FieldMetadata(obj.getString("name"), obj.getString("descriptor"), toComment(obj));
    }

    private static MetaTinyV2Mapper.MethodMetadata toMethodMetadata(final JSONObject obj) {
        MetaTinyV2Mapper.MethodMetadata methodMetadata = new MetaTinyV2Mapper.MethodMetadata(obj.getString("name"), obj.getString("descriptor"), toComment(obj), new ArrayList<>());
        for (Object parameterMeta : obj.optJSONArray("parameters", EMPTY_JSON_ARRAY)) {
            methodMetadata.getParameters().add(toParameterMetadata((JSONObject) parameterMeta));
        }
        return methodMetadata;
    }

    private static MetaTinyV2Mapper.ParameterMetadata toParameterMetadata(final JSONObject obj) {
        return new MetaTinyV2Mapper.ParameterMetadata(obj.getInt("index"), obj.getString("name"), toComment(obj));
    }

    @Nullable
    private static String toComment(final JSONObject obj) {
        Object rawJavadoc = obj.opt("javadoc");
        if (rawJavadoc == null) return null;
        String comment;
        if (rawJavadoc instanceof String) {
            comment = (String) rawJavadoc;
        } else if (rawJavadoc instanceof JSONArray) {
            comment = String.join("\\n", obj.optJSONArray("javadoc", EMPTY_JSON_ARRAY).toList().stream().map(Object::toString).toArray(String[]::new));
        } else {
            throw new IllegalStateException("Invalid comment type: " + rawJavadoc);
        }
        if (comment.isBlank()) return null;
        return comment;
    }

}
