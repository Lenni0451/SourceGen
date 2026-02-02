package net.lenni0451.sourcegen.steps.target;

import lombok.SneakyThrows;
import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.commons.lazy.Lazy;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.NetUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class IterateRetroMCPVersions extends IterateVersionsStep<IterateRetroMCPVersions.VersionData> {

    private static final DateTimeFormatter RETROMCP_FORK_TIME = DateTimeFormatter.ofPattern("yyyy-M-d'T'HH:mm:ssX"); //2009-10-24T18:04:24Z

    private final VersionStepProvider stepProvider;

    public IterateRetroMCPVersions(final File repoDir, final String branch, final VersionStepProvider stepProvider) {
        super(repoDir, branch);
        this.stepProvider = stepProvider;
    }

    @Override
    protected String getName() {
        return "RetroMCP";
    }

    @Override
    protected Collection<VersionData> loadVersions() throws IOException {
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

    @Override
    protected void processVersions(Collection<VersionData> versions) throws Exception {
        super.removeBuiltVersions(versions);
    }

    @Override
    protected String getVersionId(VersionData version) {
        return version.name();
    }

    @Override
    protected void provideSteps(List<GeneratorStep> steps, VersionData version) throws Exception {
        this.stepProvider.provideSteps(steps, version);
    }


    public record VersionData(String name, OffsetDateTime time, @Nullable String resourcesUrl, Lazy<String> clientUrl, String mappingsName, String exceptionsName) {
    }

    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> steps, final VersionData versionData) throws Exception;
    }

}
