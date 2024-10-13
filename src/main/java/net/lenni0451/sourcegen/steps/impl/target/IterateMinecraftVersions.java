package net.lenni0451.sourcegen.steps.impl.target;

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
import java.util.function.Predicate;

public class IterateMinecraftVersions implements GeneratorStep {

    private static final String INDEX_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final Exclusions EXCLUSIONS = new Exclusions(new File("exclusions", "minecraft.txt"));

    private final File repoDir;
    private final String branch;
    private final VersionRange versionRange;
    private final Predicate<String> removeVersionIf;
    private final boolean keepVersionsWithoutMappings;
    private final VersionStepProvider stepProvider;

    public IterateMinecraftVersions(final File repoDir, final String branch, final VersionStepProvider stepProvider) {
        this(repoDir, branch, new VersionRange(null, null), stepProvider);
    }

    public IterateMinecraftVersions(final File repoDir, final String branch, final VersionRange versionRange, final VersionStepProvider stepProvider) {
        this(repoDir, branch, versionRange, version -> false, false, stepProvider);
    }

    public IterateMinecraftVersions(final File repoDir, final String branch, final VersionRange versionRange, final Predicate<String> removeVersionIf, final boolean keepVersionsWithoutMappings, final VersionStepProvider stepProvider) {
        this.repoDir = repoDir;
        this.branch = branch;
        this.versionRange = versionRange;
        this.removeVersionIf = removeVersionIf;
        this.keepVersionsWithoutMappings = keepVersionsWithoutMappings;
        this.stepProvider = stepProvider;
    }

    @Override
    public void printStep() {
        System.out.println("Searching for Minecraft versions...");
    }

    @Override
    public void run() throws Exception {
        Map<OffsetDateTime, JSONObject> versions = this.loadVersions();
        this.removeBuiltVersions(versions);
        this.filterVersionRange(versions);
        this.filterPredicate(versions);
        this.resolveVersionManifest(versions);
        int i = 0;
        for (Map.Entry<OffsetDateTime, JSONObject> entry : versions.entrySet()) {
            JSONObject versionManifest = entry.getValue().getJSONObject("manifest");
            List<GeneratorStep> steps = new ArrayList<>();
            String versionName = entry.getValue().getString("id");
            this.stepProvider.provideSteps(steps, versionName, entry.getKey(), versionManifest);
            System.out.println("Running steps for version " + versionName + " (" + (++i) + "/" + versions.size() + ")...");
            long start = System.nanoTime();
            StepExecutor executor = new StepExecutor(steps);
            executor.run();
            long end = System.nanoTime();
            System.out.println("Finished steps for version " + versionName + " in " + (end - start) / 1_000_000 + "ms");
        }
    }

    private Map<OffsetDateTime, JSONObject> loadVersions() throws IOException {
        JSONObject meta = NetUtils.getJsonObject(INDEX_URL);
        JSONArray versions = meta.getJSONArray("versions");
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

    private void filterVersionRange(final Map<OffsetDateTime, JSONObject> versions) {
        if (this.versionRange.minVersion != null) {
            Iterator<Map.Entry<OffsetDateTime, JSONObject>> it = versions.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<OffsetDateTime, JSONObject> entry = it.next();
                JSONObject version = entry.getValue();
                String versionName = version.getString("id");
                if (versionName.equals(this.versionRange.minVersion)) break;
                it.remove();
            }
        }
        if (this.versionRange.maxVersion != null) {
            Iterator<Map.Entry<OffsetDateTime, JSONObject>> it = versions.entrySet().iterator();
            boolean remove = false;
            while (it.hasNext()) {
                Map.Entry<OffsetDateTime, JSONObject> entry = it.next();
                JSONObject version = entry.getValue();
                String versionName = version.getString("id");
                if (remove) it.remove();
                else if (versionName.equals(this.versionRange.maxVersion)) remove = true;
            }
        }
    }

    private void filterPredicate(final Map<OffsetDateTime, JSONObject> versions) {
        Iterator<Map.Entry<OffsetDateTime, JSONObject>> it = versions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<OffsetDateTime, JSONObject> entry = it.next();
            JSONObject version = entry.getValue();
            String versionName = version.getString("id");
            if (this.removeVersionIf.test(versionName)) it.remove();
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
            if (!downloads.has("client_mappings") && !this.keepVersionsWithoutMappings) {
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

    public record VersionRange(@Nullable String minVersion, @Nullable String maxVersion) {
    }

}
