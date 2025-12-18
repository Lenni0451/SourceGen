package net.lenni0451.sourcegen.steps.target;

import net.lenni0451.commons.gson.GsonParser;
import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonElement;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.ETA;
import net.lenni0451.sourcegen.utils.NetUtils;
import net.lenni0451.sourcegen.utils.external.Commands;

import java.io.*;
import java.time.OffsetDateTime;
import java.util.*;

public class IterateMCPVersions implements GeneratorStep {

    private final File repoDir;
    private final String branch;
    private final VersionStepProvider stepProvider;

    public IterateMCPVersions(final File repoDir, final String branch, final VersionStepProvider stepProvider) {
        this.repoDir = repoDir;
        this.branch = branch;
        this.stepProvider = stepProvider;
    }

    @Override
    public void printStep() {
        System.out.println("Searching for MCP versions...");
    }

    @Override
    public void run() throws Exception {
        List<GsonObject> versions = this.loadVersions();
        this.removeBuiltVersions(versions);
        this.resolveClientManifest(versions);
        this.resolveVersionManifest(versions);

        int i = 0;
        ETA eta = new ETA();
        for (GsonObject entry : versions) {
            List<String> mappingsDownloads = new ArrayList<>();
            if (entry.get("url") instanceof GsonArray) {
                for (GsonElement mapping : entry.getArray("url")) {
                    mappingsDownloads.add(mapping.asString());
                }
            } else if (entry.get("url").isPrimitive()) {
                mappingsDownloads.add(entry.getString("url"));
            } else {
                throw new IllegalStateException("Invalid mappings format in MCP version entry: " + entry);
            }
            List<GeneratorStep> steps = new ArrayList<>();
            String versionName = entry.getString("id");
            this.stepProvider.provideSteps(steps, versionName, OffsetDateTime.parse(entry.getString("releaseTime")), entry.getString("client"), mappingsDownloads, entry.getString("mappings"));
            System.out.println("Running steps for version " + versionName + " (" + (++i) + "/" + versions.size() + (eta.canEstimate() ? (" ETA: " + ETA.format(eta.getNextEstimation()) + "/" + ETA.format(eta.getEstimation(versions.size() - (i - 1)))) : "") + ")...");
            eta.start();
            StepExecutor executor = new StepExecutor(steps);
            executor.run();
            eta.stop();
            System.out.println("Finished steps for version " + versionName + " in " + ETA.format(eta.getLastDuration()));
        }
    }

    private List<GsonObject> loadVersions() throws IOException {
        File mcpsFile = new File("mcps.json");
        if (!mcpsFile.exists()) {
            throw new IOException("Could not find MCP versions file: " + mcpsFile.getAbsolutePath() + ". This file needs to be manually supplied.");
        }
        try (InputStream is = new FileInputStream(mcpsFile)) {
            List<GsonObject> versions = new ArrayList<>();
            GsonArray array = GsonParser.parse(new InputStreamReader(is)).asArray();
            for (GsonElement gsonElement : array) {
                versions.add(gsonElement.asObject());
            }
            return versions;
        }
    }

    private void removeBuiltVersions(final List<GsonObject> versions) throws IOException {
        String lastBuiltVersion = Commands.git(this.repoDir).latestCommitMessage(this.branch);
        boolean hasVersion = versions.stream().map(v -> v.getString("id")).toList().contains(lastBuiltVersion);
        if (!hasVersion) return;
        Iterator<GsonObject> it = versions.iterator();
        while (it.hasNext()) {
            GsonObject version = it.next();
            String versionName = version.getString("id");
            it.remove();
            if (versionName.equalsIgnoreCase(lastBuiltVersion)) break;
        }
    }

    private void resolveClientManifest(final List<GsonObject> versions) throws IOException {
        GsonObject meta = NetUtils.getJsonObject(Config.OnlineResources.minecraftVersionManifest);
        GsonArray versionManifests = meta.getArray("versions");
        Map<String, VersionInfo> versionManifestURLs = new HashMap<>();
        for (int i = 0; i < versionManifests.size(); i++) {
            GsonObject versionManifest = versionManifests.getObject(i);
            String id = versionManifest.getString("id");
            String releaseTime = versionManifest.getString("releaseTime");
            String url = versionManifest.getString("url");
            versionManifestURLs.put(id, new VersionInfo(releaseTime, url));
        }
        for (GsonObject version : versions) {
            String versionName = version.getString("id");
            VersionInfo info = versionManifestURLs.get(versionName);
            if (info == null) throw new IllegalStateException("Could not find version manifest URL for version " + versionName);
            version.add("releaseTime", info.releaseTime);
            version.add("manifestUrl", info.manifestUrl);
        }
    }

    private void resolveVersionManifest(final List<GsonObject> versions) throws IOException {
        for (GsonObject version : versions) {
            String url = version.getString("manifestUrl");
            GsonObject versionManifest = NetUtils.getJsonObject(url);
            GsonObject downloads = versionManifest.getObject("downloads");
            GsonObject client = downloads.getObject("client");
            version.add("client", client.getString("url"));
        }
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> versionSteps, final String versionName, final OffsetDateTime releaseTime, final String clientURL, final List<String> mappingsDownloads, final String mappingsVersion) throws Exception;
    }

    private record VersionInfo(String releaseTime, String manifestUrl) {
    }

}
