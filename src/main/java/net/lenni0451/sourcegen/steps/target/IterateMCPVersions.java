package net.lenni0451.sourcegen.steps.target;

import net.lenni0451.commons.gson.GsonParser;
import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonElement;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.NetUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.*;

public class IterateMCPVersions extends IterateVersionsStep<GsonObject> {

    private final VersionStepProvider stepProvider;

    public IterateMCPVersions(final File repoDir, final String branch, final VersionStepProvider stepProvider) {
        super(repoDir, branch);
        this.stepProvider = stepProvider;
    }

    @Override
    protected String getName() {
        return "MCP";
    }

    @Override
    protected Collection<GsonObject> loadVersions() throws IOException {
        try (InputStream is = IterateMCPVersions.class.getClassLoader().getResourceAsStream("mcps.json")) {
            List<GsonObject> versions = new ArrayList<>();
            GsonArray array = GsonParser.parse(new InputStreamReader(is)).asArray();
            for (GsonElement gsonElement : array) {
                versions.add(gsonElement.asObject());
            }
            return versions;
        }
    }

    @Override
    protected void processVersions(Collection<GsonObject> versions) throws Exception {
        super.removeBuiltVersions(versions);
        this.resolveClientManifest(versions);
        this.resolveVersionManifest(versions);
    }

    @Override
    protected String getVersionId(GsonObject version) {
        return version.getString("id");
    }

    @Override
    protected void provideSteps(List<GeneratorStep> steps, GsonObject version) throws Exception {
        List<String> mappingsDownloads = new ArrayList<>();
        if (version.get("url") instanceof GsonArray) {
            for (GsonElement mapping : version.getArray("url")) {
                mappingsDownloads.add(mapping.asString());
            }
        } else if (version.get("url").isPrimitive()) {
            mappingsDownloads.add(version.getString("url"));
        } else {
            throw new IllegalStateException("Invalid mappings format in MCP version entry: " + version);
        }
        String versionName = version.getString("id");
        this.stepProvider.provideSteps(steps, versionName, OffsetDateTime.parse(version.getString("releaseTime")), version.getString("client"), mappingsDownloads, version.getString("mappings"));
    }

    private void resolveClientManifest(final Collection<GsonObject> versions) throws IOException {
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

    private void resolveVersionManifest(final Collection<GsonObject> versions) throws IOException {
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
