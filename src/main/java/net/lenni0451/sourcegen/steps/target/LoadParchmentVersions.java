package net.lenni0451.sourcegen.steps.target;

import net.lenni0451.commons.gson.elements.GsonArray;
import net.lenni0451.commons.gson.elements.GsonObject;
import net.lenni0451.sourcegen.Config;
import net.lenni0451.sourcegen.steps.GeneratorStep;
import net.lenni0451.sourcegen.steps.StepExecutor;
import net.lenni0451.sourcegen.utils.NetUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LoadParchmentVersions implements GeneratorStep {

    private final VersionStepProvider stepProvider;

    public LoadParchmentVersions(final VersionStepProvider stepProvider) {
        this.stepProvider = stepProvider;
    }

    @Override
    public void printStep() {
        System.out.println("Loading Parchment mappings...");
    }

    @Override
    public void run() throws Exception {
        List<String> rawVersions = this.getVersions();
        Map<String, String> parsedVersions = this.getVersionUrls(rawVersions);

        List<GeneratorStep> steps = new ArrayList<>();
        this.stepProvider.provideSteps(steps, version -> parsedVersions.get("parchment-" + version));
        StepExecutor executor = new StepExecutor(steps);
        executor.run();
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
            String latestVersion = this.getLatestVersion(metadataUrl);
            urls.put(version, Config.OnlineResources.getParchmentMappings(version + "/" + latestVersion + "/" + version + "-" + latestVersion + ".zip"));
        }
        return urls;
    }

    private String getLatestVersion(final String metadataUrl) throws Exception {
        String xml = new String(NetUtils.get(metadataUrl), StandardCharsets.UTF_8);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return document.getElementsByTagName("latest").item(0).getTextContent();
    }


    @FunctionalInterface
    public interface VersionStepProvider {
        void provideSteps(final List<GeneratorStep> subSteps, final Function<String, String> versionToUrl);
    }

}
