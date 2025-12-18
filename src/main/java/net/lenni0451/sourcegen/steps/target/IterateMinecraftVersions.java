package net.lenni0451.sourcegen.steps.target;

import lombok.RequiredArgsConstructor;
import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.ETA;
import net.lenni0451.sourcegen.utils.NetUtils;
import net.lenni0451.sourcegen.utils.external.Commands;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class IterateMinecraftVersions implements GeneratorStep {

    private final File repoDir;
    private final String branch;
    private final VersionRange versionRange;
    private final Predicate<GsonObject> removeVersionIf;
    private final boolean keepVersionsWithoutMappings;
    private final boolean ignoreExclusions;
    private final VersionStepProvider stepProvider;

    @Override
    public void printStep() {
        System.out.println("Searching for Minecraft versions...");
    }

    @Override
    public void run() throws Exception {
        Map<OffsetDateTime, GsonObject> versions = this.loadVersions();
        this.filterVersionRange(versions);
        this.removeBuiltVersions(versions);
        this.filterPredicate(versions);
        this.resolveVersionManifest(versions);

        int i = 0;
        ETA eta = new ETA();
        for (Map.Entry<OffsetDateTime, GsonObject> entry : versions.entrySet()) {
            GsonObject versionManifest = entry.getValue().getObject("manifest");
            List<GeneratorStep> steps = new ArrayList<>();
            String versionName = entry.getValue().getString("id");
            this.stepProvider.provideSteps(steps, versionName, entry.getKey(), versionManifest);
            System.out.println("Running steps for version " + versionName + " (" + (++i) + "/" + versions.size() + (eta.canEstimate() ? (" ETA: " + ETA.format(eta.getNextEstimation()) + "/" + ETA.format(eta.getEstimation(versions.size() - (i - 1)))) : "") + ")...");
            eta.start();
            StepExecutor executor = new StepExecutor(steps);
            executor.run();
            eta.stop();
            System.out.println("Finished steps for version " + versionName + " in " + ETA.format(eta.getLastDuration()));
        }
    }

    private Map<OffsetDateTime, GsonObject> loadVersions() throws IOException {
        GsonObject meta = NetUtils.getJsonObject(Config.OnlineResources.minecraftVersionManifest);
        GsonArray versions = meta.getArray("versions");
        Map<OffsetDateTime, GsonObject> sortedVersions = new TreeMap<>();
        for (int i = 0; i < versions.size(); i++) {
            GsonObject version = versions.getObject(i);
            if (!this.ignoreExclusions && Config.Exclusions.minecraft.contains(version.getString("id"))) continue;

            String time = version.getString("releaseTime");
            sortedVersions.put(OffsetDateTime.parse(time), version);
        }
        return sortedVersions;
    }

    private void removeBuiltVersions(final Map<OffsetDateTime, GsonObject> versions) throws IOException {
        String lastBuiltVersion = Commands.git(this.repoDir).latestCommitMessage(this.branch);
        boolean hasVersion = versions.values().stream().map(v -> v.getString("id")).toList().contains(lastBuiltVersion);
        if (!hasVersion) return;
        Iterator<Map.Entry<OffsetDateTime, GsonObject>> it = versions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<OffsetDateTime, GsonObject> entry = it.next();
            GsonObject version = entry.getValue();
            String versionName = version.getString("id");
            it.remove();
            if (versionName.equalsIgnoreCase(lastBuiltVersion)) break;
        }
    }

    private void filterVersionRange(final Map<OffsetDateTime, GsonObject> versions) {
        if (this.versionRange.minVersion != null) {
            Iterator<Map.Entry<OffsetDateTime, GsonObject>> it = versions.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<OffsetDateTime, GsonObject> entry = it.next();
                GsonObject version = entry.getValue();
                String versionName = version.getString("id");
                if (versionName.equals(this.versionRange.minVersion)) break;
                it.remove();
            }
        }
        if (this.versionRange.maxVersion != null) {
            Iterator<Map.Entry<OffsetDateTime, GsonObject>> it = versions.entrySet().iterator();
            boolean remove = false;
            while (it.hasNext()) {
                Map.Entry<OffsetDateTime, GsonObject> entry = it.next();
                GsonObject version = entry.getValue();
                String versionName = version.getString("id");
                if (remove) it.remove();
                else if (versionName.equals(this.versionRange.maxVersion)) remove = true;
            }
        }
    }

    private void filterPredicate(final Map<OffsetDateTime, GsonObject> versions) {
        Iterator<Map.Entry<OffsetDateTime, GsonObject>> it = versions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<OffsetDateTime, GsonObject> entry = it.next();
            GsonObject version = entry.getValue();
            if (this.removeVersionIf.test(version)) it.remove();
        }
    }

    private void resolveVersionManifest(final Map<OffsetDateTime, GsonObject> versions) throws IOException {
        Iterator<Map.Entry<OffsetDateTime, GsonObject>> it = versions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<OffsetDateTime, GsonObject> entry = it.next();
            GsonObject version = entry.getValue();
            String url = version.getString("url");
            GsonObject versionManifest = NetUtils.getJsonObject(url);
            GsonObject downloads = versionManifest.getObject("downloads");
            if (!downloads.has("client_mappings") && !this.keepVersionsWithoutMappings) {
                it.remove();
            } else {
                version.add("manifest", versionManifest);
            }
        }
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> versionSteps, final String versionName, final OffsetDateTime releaseTime, final GsonObject manifest) throws Exception;
    }

    public record VersionRange(@Nullable String minVersion, @Nullable String maxVersion) {
    }

}
