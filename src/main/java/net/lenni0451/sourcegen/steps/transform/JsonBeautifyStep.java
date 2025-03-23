package net.lenni0451.sourcegen.steps.transform;

import net.lenni0451.commons.io.FileUtils;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;

public class JsonBeautifyStep implements GeneratorStep {

    private final File repoDir;

    public JsonBeautifyStep(final File repoDir) {
        this.repoDir = repoDir;
    }

    @Override
    public void printStep() {
        System.out.println("Beautifying json files...");
    }

    @Override
    public void run() throws Exception {
        for (File file : FileUtils.listFiles(this.repoDir)) {
            if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".json")) continue;
            try {
                Object json;
                try (FileInputStream fis = new FileInputStream(file)) {
                    json = new JSONTokener(fis).nextValue();
                }
                if (json instanceof JSONObject jsonObject) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(jsonObject.toString(4).getBytes());
                    }
                } else if (json instanceof JSONArray jsonArray) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(jsonArray.toString(4).getBytes());
                    }
                }
            } catch (Throwable t) {
                System.out.println("Failed to beautify json file " + file.getAbsolutePath() + ": " + t.getMessage());
            }
        }
    }

}
