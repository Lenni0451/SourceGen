package net.lenni0451.sourcegen.steps.target;

import lombok.SneakyThrows;
import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.commons.lazy.Lazy;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

public class IterateRetroMCPVersions implements GeneratorStep {

    private static final DateTimeFormatter RETROMCP_FORK_TIME = DateTimeFormatter.ofPattern("yyyy-M-d'T'HH:mm:ssX"); //2009-10-24T18:04:24Z

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
        Set<VersionData> versions = this.loadVersions();
        this.removeBuiltVersions(versions);

        int i = 0;
        ETA eta = new ETA();
        for (VersionData versionData : versions) {
            List<GeneratorStep> steps = new ArrayList<>();
            this.stepProvider.provideSteps(steps, versionData);
            System.out.println("Running steps for version " + versionData.name + " (" + (++i) + "/" + versions.size() + (eta.canEstimate() ? (" ETA: " + ETA.format(eta.getNextEstimation()) + "/" + ETA.format(eta.getEstimation(versions.size() - (i - 1)))) : "") + ")...");
            eta.start();
            StepExecutor executor = new StepExecutor(steps);
            executor.run();
            eta.stop();
            System.out.println("Finished steps for version " + versionData.name + " in " + ETA.format(eta.getLastDuration()));
        }
    }

    private Set<VersionData> loadVersions() throws IOException {
        Set<String> addedVersions = new HashSet<>();
        Set<VersionData> sortedVersions = new TreeSet<>(Comparator.comparing(o -> o.time));
        { //RetroMCP
            GsonArray versions = NetUtils.getJsonArray(Config.OnlineResources.retroMCPVersions);
            for (int i = 0; i < versions.size(); i++) {
                GsonObject version = versions.getObject(i);
                String id = version.getString("id");
                if (Config.Exclusions.retroMCP.contains(id)) continue;
                if (!addedVersions.add(id)) continue;

                String time = version.getString("releaseTime");
                String resources = version.getString("resources");
                sortedVersions.add(new VersionData(id, OffsetDateTime.parse(time), resources, new Lazy<>() {
                    @Override
                    @SneakyThrows
                    protected String calculate() {
                        String url = version.getString("url");
                        GsonObject versionManifest = NetUtils.getJsonObject(url);
                        GsonObject downloads = versionManifest.getObject("downloads");
                        GsonObject client = downloads.getObject("client");
                        return client.getString("url");
                    }
                }, "mappings.tiny", "exceptions.exc"));
            }
        }
        { //RetroMCP fork
            GsonObject versions = NetUtils.getJsonObject(Config.OnlineResources.retroMCPForkVersions);
            for (String id : versions.keySet()) {
                GsonObject version = versions.getObject(id);
                if (Config.Exclusions.retroMCPFork.contains(id)) continue;
                if (!addedVersions.add(id)) continue;

                String time = version.getString("client_timestamp");
                String resources = version.getString("resources");
                if (resources != null) resources = Config.OnlineResources.retroMCPForkData + resources;
                sortedVersions.add(new VersionData(id, OffsetDateTime.parse(time, RETROMCP_FORK_TIME), resources, new Lazy<>() {
                    @Override
                    @SneakyThrows
                    protected String calculate() {
                        return version.getString("client_url");
                    }
                }, "client.tiny", "client.exc"));
            }
        }
        return sortedVersions;
    }

    private void removeBuiltVersions(final Set<VersionData> versions) throws IOException {
        String lastBuiltVersion = Commands.git(this.repoDir).latestCommitMessage(this.branch);
        boolean hasVersion = versions.stream().map(v -> v.name).toList().contains(lastBuiltVersion);
        if (!hasVersion) return;
        Iterator<VersionData> it = versions.iterator();
        while (it.hasNext()) {
            VersionData versionData = it.next();
            it.remove();
            if (versionData.name.equalsIgnoreCase(lastBuiltVersion)) break;
        }
    }


    public record VersionData(String name, OffsetDateTime time, @Nullable String resourcesUrl, Lazy<String> clientUrl, String mappingsName, String exceptionsName) {
    }

    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> steps, final VersionData versionData) throws Exception;
    }

}
