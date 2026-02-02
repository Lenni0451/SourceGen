package net.lenni0451.sourcegen.steps.target;

import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.utils.NetUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LoadParchmentVersions extends LoadContextStep<Function<String, String>> {

    private final VersionStepProvider stepProvider;

    public LoadParchmentVersions(final VersionStepProvider stepProvider) {
        this.stepProvider = stepProvider;
    }

    @Override
    public void printStep() {
        System.out.println("Loading Parchment mappings...");
    }

    @Override
    protected Function<String, String> loadContext() throws Exception {
        List<String> rawVersions = this.getVersions();
        Map<String, String> parsedVersions = this.getVersionUrls(rawVersions);
        return version -> parsedVersions.get("parchment-" + version);
    }

    @Override
    protected void provideSteps(List<GeneratorStep> steps, Function<String, String> context) throws Exception {
        this.stepProvider.provideSteps(steps, context);
    }

    private List<String> getVersions() throws Exception {
        List<String> versions = new ArrayList<>();
        GsonObject json = NetUtils.getJsonObject(Config.OnlineResources.parchmentMetadata);
        GsonArray data = json.getArray("data");
        for (int i = 0; i < data.size(); i++) {
            GsonObject version = data.getObject(i);
            String name = version.getString("name");
            if (name.startsWith("parchment-")) versions.add(name);
        }
        return versions;
    }

    private Map<String, String> getVersionUrls(final List<String> versions) throws Exception {
        Map<String, String> urls = new HashMap<>();
        for (String version : versions) {
            String metadataUrl = Config.OnlineResources.getParchmentMappings(version + "/maven-metadata.xml");
            String latestVersion = NetUtils.getMavenLatestVersion(metadataUrl);
            urls.put(version, Config.OnlineResources.getParchmentMappings(version + "/" + latestVersion + "/" + version + "-" + latestVersion + ".zip"));
        }
        return urls;
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> subSteps, final Function<String, String> versionToUrl);
    }

}
