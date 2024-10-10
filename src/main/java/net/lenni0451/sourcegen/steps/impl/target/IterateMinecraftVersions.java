package net.lenni0451.sourcegen.steps.impl.target;

import net.lenni0451.commons.Sneaky;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.NetUtils;
import net.lenni0451.sourcegen.utils.external.Commands;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.*;

public class IterateMinecraftVersions implements GeneratorStep {

    private static final String MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final List<String> EXCLUDED;

    static {
        try {
            EXCLUDED = Files.readAllLines(new File("minecraft_excluded.txt").toPath());
        } catch (IOException e) {
            Sneaky.sneak(e);
            throw new RuntimeException(e);
        }
    }

    private final File repoDir;
    private final String branch;
    private final VersionStepProvider stepSupplier;

    public IterateMinecraftVersions(final File repoDir, final String branch, final VersionStepProvider stepSupplier) {
        this.repoDir = repoDir;
        this.branch = branch;
        this.stepSupplier = stepSupplier;
    }

    @Override
    public void printStep() {
        System.out.println("Searching for Minecraft versions...");
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
            this.stepSupplier.provideSteps(steps, versionName, entry.getKey(), versionManifest);
            System.out.println("Running steps for version " + versionName + " (" + (++i) + "/" + versions.size() + ")...");
            long start = System.nanoTime();
            StepExecutor executor = new StepExecutor(steps);
            executor.run();
            long end = System.nanoTime();
            System.out.println("Finished steps for version " + versionName + " in " + (end - start) / 1_000_000 + "ms");
        }
    }

    private Map<OffsetDateTime, JSONObject> loadVersions() throws IOException {
        JSONObject meta = NetUtils.getJsonObject(MANIFEST);
        JSONArray versions = meta.getJSONArray("versions");
        Map<OffsetDateTime, JSONObject> sortedVersions = new TreeMap<>();
        for (int i = 0; i < versions.length(); i++) {
            JSONObject version = versions.getJSONObject(i);
            if (EXCLUDED.contains(version.getString("id"))) continue;

            String time = version.getString("releaseTime");
            sortedVersions.put(OffsetDateTime.parse(time), version);
        }
        return sortedVersions;
    }

    private void removeBuiltVersions(final Map<OffsetDateTime, JSONObject> versions) throws IOException {
        String lastBuiltVersion = Commands.git(this.repoDir).latestCommitMessage(this.branch);
        boolean hasVersion = versions.values().stream().map(v -> v.getString("id")).toList().contains(lastBuiltVersion);
        if (!hasVersion) lastBuiltVersion = "1.13.2";
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
        Iterator<Map.Entry<OffsetDateTime, JSONObject>> it = versions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<OffsetDateTime, JSONObject> entry = it.next();
            JSONObject version = entry.getValue();
            String url = version.getString("url");
            JSONObject versionManifest = NetUtils.getJsonObject(url);
            JSONObject downloads = versionManifest.getJSONObject("downloads");
            if (!downloads.has("client_mappings")) {
                //If a version does not have mappings, remove it
                it.remove();
            } else {
                version.put("manifest", versionManifest);
            }
        }
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> versionSteps, final String versionName, final OffsetDateTime releaseTime, final JSONObject manifest) throws Exception;
    }

}
