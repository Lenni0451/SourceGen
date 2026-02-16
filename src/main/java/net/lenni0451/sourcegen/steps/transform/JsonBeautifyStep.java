package net.lenni0451.sourcegen.steps.transform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import net.lenni0451.commons.gson.GsonParser;
import net.lenni0451.commons.gson.elements.GsonElement;
import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.steps.GeneratorStep;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Locale;

@Slf4j
public class JsonBeautifyStep implements GeneratorStep {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final File repoDir;

    public JsonBeautifyStep(final File repoDir) {
        this.repoDir = repoDir;
    }

    @Override
    public void printStep() {
        log.info("Beautifying json files...");
    }

    @Override
    public void run() throws Exception {
        for (File file : FileUtils.listFiles(this.repoDir)) {
            if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".json")) continue;
            try {
                GsonElement element = GsonParser.parse(Files.readString(file.toPath()));
                StringWriter stringWriter = new StringWriter();
                JsonWriter jsonWriter = GSON.newJsonWriter(stringWriter);
                jsonWriter.setIndent(" ".repeat(4));
                GSON.toJson(element.getJsonElement(), jsonWriter);
                jsonWriter.close();
                Files.writeString(file.toPath(), stringWriter.toString());
            } catch (Throwable t) {
                log.error("Failed to beautify json file {}: {}", file.getAbsolutePath(), t.getMessage());
            }
        }
    }

}
