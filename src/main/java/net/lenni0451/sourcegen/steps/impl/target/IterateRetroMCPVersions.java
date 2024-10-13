package net.lenni0451.sourcegen.steps.impl.target;

import net.lenni0451.sourcegen.Main;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.Exclusions;
import net.lenni0451.sourcegen.utils.NetUtils;
import net.lenni0451.sourcegen.utils.external.Commands;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

public class IterateRetroMCPVersions implements GeneratorStep {

    private static final String INDEX_URL = "https://mcphackers.org/versionsV2/versions.json";
    private static final Exclusions EXCLUSIONS = new Exclusions(new File(Main.EXCLUSIONS_DIR, "retromcp.txt"));

    private final File repoDir;
    private final String branch;
    private final VersionStepProvider stepProvider;

    public IterateRetroMCPVersions(final File repoDir, final String branch, final VersionStepProvider stepProvider) {
        this.repoDir = repoDir;
        this.branch = branch;
        this.stepProvider = stepProvider;
    }

    @Override
    public void printStep() {
        System.out.println("Searching for RetroMCP versions...");
    }

    @Override
    public void run() throws Exception {
        Map<OffsetDateTime, JSONObject> versions = this.loadVersions();
        this.removeBuiltVersions(versions);
        this.resolveVersionManifest(versions);
        int i = 0;
        for (Map.Entry<OffsetDateTime, JSONObject> entry : versions.entrySet()) {
            JSONObject versionManifest = entry.getValue().getJSONObject("manifest");
            List<GeneratorStep> steps = new ArrayList<>();
            String versionName = entry.getValue().getString("id");
            this.stepProvider.provideSteps(steps, versionName, entry.getKey(), entry.getValue().optString("resources", null), versionManifest);
            System.out.println("Running steps for version " + versionName + " (" + (++i) + "/" + versions.size() + ")...");
            long start = System.nanoTime();
            StepExecutor executor = new StepExecutor(steps);
            executor.run();
            long end = System.nanoTime();
            System.out.println("Finished steps for version " + versionName + " in " + (end - start) / 1_000_000 + "ms");
        }
    }

    private Map<OffsetDateTime, JSONObject> loadVersions() throws IOException {
        JSONArray versions = NetUtils.getJsonArray(INDEX_URL);
        Map<OffsetDateTime, JSONObject> sortedVersions = new TreeMap<>();
        for (int i = 0; i < versions.length(); i++) {
            JSONObject version = versions.getJSONObject(i);
            if (EXCLUSIONS.isExcluded(version.getString("id"))) continue;

            String time = version.getString("releaseTime");
            sortedVersions.put(OffsetDateTime.parse(time), version);
        }
        return sortedVersions;
    }

    private void removeBuiltVersions(final Map<OffsetDateTime, JSONObject> versions) throws IOException {
        String lastBuiltVersion = Commands.git(this.repoDir).latestCommitMessage(this.branch);
        boolean hasVersion = versions.values().stream().map(v -> v.getString("id")).toList().contains(lastBuiltVersion);
        if (!hasVersion) return;
        Iterator<Map.Entry<OffsetDateTime, JSONObject>> it = versions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<OffsetDateTime, JSONObject> entry = it.next();
            JSONObject version = entry.getValue();
            String versionName = version.getString("id");
            it.remove();
            if (versionName.equalsIgnoreCase(lastBuiltVersion)) break;
        }
    }

    private void resolveVersionManifest(final Map<OffsetDateTime, JSONObject> versions) throws IOException {
        for (Map.Entry<OffsetDateTime, JSONObject> entry : versions.entrySet()) {
            JSONObject version = entry.getValue();
            String url = version.getString("url");
            JSONObject versionManifest = NetUtils.getJsonObject(url);
            version.put("manifest", versionManifest);
        }
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> steps, final String versionName, final OffsetDateTime releaseTime, @Nullable final String resourcesUrl, final JSONObject manifest) throws Exception;
    }

}
