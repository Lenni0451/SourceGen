package net.lenni0451.sourcegen.steps.target;

import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.ETA;
import net.lenni0451.sourcegen.utils.NetUtils;
import net.lenni0451.sourcegen.utils.external.Commands;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        List<JSONObject> versions = this.loadVersions();
        this.removeBuiltVersions(versions);
        this.resolveClientManifest(versions);
        this.resolveVersionManifest(versions);

        int i = 0;
        ETA eta = new ETA();
        for (JSONObject entry : versions) {
            List<String> mappingsDownloads = new ArrayList<>();
            if (entry.get("url") instanceof JSONArray) {
                JSONArray mappings = entry.getJSONArray("url");
                for (Object mapping : mappings) {
                    mappingsDownloads.add(mapping.toString());
                }
            } else if (entry.get("url") instanceof String) {
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

    private List<JSONObject> loadVersions() throws IOException {
        File mcpsFile = new File("mcps.json");
        if (!mcpsFile.exists()) {
            throw new IOException("Could not find MCP versions file: " + mcpsFile.getAbsolutePath() + ". This file needs to be manually supplied.");
        }
        try (InputStream is = new FileInputStream(mcpsFile)) {
            List<JSONObject> versions = new ArrayList<>();
            JSONArray array = new JSONArray(new JSONTokener(is));
            for (Object o : array) versions.add((JSONObject) o);
            return versions;
        }
    }

    private void removeBuiltVersions(final List<JSONObject> versions) throws IOException {
        String lastBuiltVersion = Commands.git(this.repoDir).latestCommitMessage(this.branch);
        boolean hasVersion = versions.stream().map(v -> v.getString("id")).toList().contains(lastBuiltVersion);
        if (!hasVersion) return;
        Iterator<JSONObject> it = versions.iterator();
        while (it.hasNext()) {
            JSONObject version = it.next();
            String versionName = version.getString("id");
            it.remove();
            if (versionName.equalsIgnoreCase(lastBuiltVersion)) break;
        }
    }

    private void resolveClientManifest(final List<JSONObject> versions) throws IOException {
        JSONObject meta = NetUtils.getJsonObject(Config.OnlineResources.minecraftVersionManifest);
        JSONArray versionManifests = meta.getJSONArray("versions");
        Map<String, VersionInfo> versionManifestURLs = new HashMap<>();
        for (int i = 0; i < versionManifests.length(); i++) {
            JSONObject versionManifest = versionManifests.getJSONObject(i);
            String id = versionManifest.getString("id");
            String releaseTime = versionManifest.getString("releaseTime");
            String url = versionManifest.getString("url");
            versionManifestURLs.put(id, new VersionInfo(releaseTime, url));
        }
        for (JSONObject version : versions) {
            String versionName = version.getString("id");
            VersionInfo info = versionManifestURLs.get(versionName);
            if (info == null) throw new IllegalStateException("Could not find version manifest URL for version " + versionName);
            version.put("releaseTime", info.releaseTime);
            version.put("manifestUrl", info.manifestUrl);
        }
    }

    private void resolveVersionManifest(final List<JSONObject> versions) throws IOException {
        for (JSONObject version : versions) {
            String url = version.getString("manifestUrl");
            JSONObject versionManifest = NetUtils.getJsonObject(url);
            JSONObject downloads = versionManifest.getJSONObject("downloads");
            JSONObject client = downloads.getJSONObject("client");
            version.put("client", client.getString("url"));
        }
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> versionSteps, final String versionName, final OffsetDateTime releaseTime, final String clientURL, final List<String> mappingsDownloads, final String mappingsVersion) throws Exception;
    }

    private record VersionInfo(String releaseTime, String manifestUrl) {
    }

}
